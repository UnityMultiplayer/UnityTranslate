package xyz.bluspring.unitytranslate.common.translator

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1MarkIncompletePacket
import xyz.bluspring.unitytranslate.common.translator.instances.LibreTranslateInstance
import xyz.bluspring.unitytranslate.common.translator.instances.LocalTranslationInstance
import xyz.bluspring.unitytranslate.common.translator.instances.TranslationInstance
import xyz.bluspring.unitytranslate.common.util.IgnoredException
import xyz.bluspring.unitytranslate.common.util.nativeaccess.CudaHelper
import xyz.bluspring.unitytranslate.library.util.collect
import xyz.bluspring.unitytranslate.library.util.concurrent
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue

class TranslatorManager(val instance: UnityTranslate) {
    private var timer: Timer = Timer("UnityTranslate Batch Translate Manager")
    internal val queuedTranslations = ConcurrentLinkedQueue<Translation>()

    private val MULTI_ASTERISK_REGEX = Regex("\\*+")
    private val MULTI_MUSIC_NOTE_REGEX = Regex("[♩♪♫♬♭♮♯°ø\u0602≠≭]+")

    val scope = CoroutineScope(Dispatchers.Default)
    val instances = ConcurrentLinkedDeque<TranslationInstance>()

    suspend fun queueTranslation(line: String, from: Language, to: Language, player: UUID, index: Int): String? {
        return try {
            val id = "$player-$index"

            for (previous in queuedTranslations.filter { it.id == id && it.fromLang == from && it.toLang == to }) {
                previous.job.completeExceptionally(IgnoredException("Overridden"))
                queuedTranslations.remove(previous)
            }

            val completable = CompletableDeferred<String>()
            queuedTranslations.add(Translation(id, line, from, to, System.currentTimeMillis(), completable, player, index))

            completable.await().replace(MULTI_ASTERISK_REGEX, "**")
                .replace(MULTI_MUSIC_NOTE_REGEX, "")
        } catch (_: IgnoredException) { // Ignored exceptions should be, well, ignored.
            null
        } catch (e: Throwable) {
            UnityTranslate.logger.error("Failed to translate line from ${from.code} to ${to.code} ($line)!")
            e.printStackTrace()
            null
        }
    }

    private fun getFirstFreeInstance(from: Language, to: Language): TranslationInstance? {
        for (instance in instances) {
            if (!instance.supportsLanguage(from, to))
                continue

            if (instance.currentlyTranslating < MAX_TRANSLATION_TASKS)
                return instance
        }

        return null
    }

    fun loadFromConfig() {
        val translatePriority = instance.config.server.translatePriority
        val list = mutableListOf<TranslationInstance>()

        for (priority in translatePriority) {
            if (((priority.isClient && instance.proxy.isClient()) || (priority.isServer && !instance.proxy.isClient()))) { // Local instances (client/server)
                if (priority.isCuda && CudaHelper.isCudaSupported && instance.config.common.shouldUseCuda)
                    list.add(LocalTranslationInstance(instance, true))
                else if (!priority.isCuda)
                    list.add(LocalTranslationInstance(instance, false))
            } else if (priority == UnityTranslateConfig.TranslationPriority.OFFLOADED &&
                (!instance.proxy.isClient() || !instance.proxy.serverSupportsTranslations())
            ) { // Offload instances (server-only, unless client-sided on an unsupported server)
                val offloadServers = instance.config.server.offloadServers.sortedByDescending { it.weight }

                for (offloadServer in offloadServers) {
                    list.add(LibreTranslateInstance(instance, offloadServer.url, offloadServer.weight, offloadServer.authKey))
                }
            }
        }

        runBlocking(Dispatchers.IO) {
            instance.library.packageIndex.indexList.asFlow().concurrent().collect { index ->
                index.packages.asFlow().concurrent(4).collect { pkg ->
                    UnityTranslate.logger.info("Downloading package ${pkg.fromCode}-${pkg.toCode}")
                    index.getOrDownloadModelInfos(pkg.fromCode, pkg.toCode)
                    UnityTranslate.logger.info("Downloaded package ${pkg.fromCode}-${pkg.toCode}")
                }
            }
        }

        timer.cancel()
        timer = Timer("UnityTranslate Batch Translate Manager")

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    val queueLater = ConcurrentLinkedQueue<Translation>()
                    val toTranslate = mutableMapOf<Pair<Language, Language>, MutableList<Translation>>()

                    // Split all translations by languages
                    while (queuedTranslations.isNotEmpty()) {
                        val translation = queuedTranslations.remove()
                        toTranslate.computeIfAbsent(translation.fromLang to translation.toLang) { mutableListOf() }
                            .add(translation)
                    }

                    toTranslate.forEach { (from, to), allTranslations ->
                        val translations = allTranslations.filter { instance.proxy.doesPlayerExist(it.playerUUID) }

                        translations.chunked(MAX_TRANSLATION_BATCH)
                            .forEach { spliced ->
                                val translatorInstance = getFirstFreeInstance(from, to)

                                // All available instances are busy, defer for later.
                                if (translatorInstance == null) {
                                    queueLater.addAll(spliced)
                                    return@forEach
                                }

                                // Start a new coroutine for translating this batch.
                                scope.launch(start = CoroutineStart.UNDISPATCHED) {
                                    val translated = translatorInstance.batchTranslate(spliced.map { it.text }, from, to)

                                    // Translation failed, mark them as failed and requeue them.
                                    // The client is expected to display a warning icon if the translation has failed.
                                    if (translated == null) {
                                        for (translation in spliced) {
                                            if (queuedTranslations.any { it.id == translation.id && it.queueTime > translation.queueTime } || translation.attempts > 3)
                                                continue

                                            instance.proxy.broadcastPacketServer(V1MarkIncompletePacket(translation.playerUUID, translation.fromLang, translation.toLang, translation.index, true))
                                            translation.attempts++
                                            queuedTranslations.add(translation)
                                        }

                                        return@launch
                                    }

                                    if (translated.size != spliced.size)
                                        throw IllegalStateException("Translated output size does not match input size! (${translated.size} != ${spliced.size})")

                                    for ((index, string) in translated.withIndex()) {
                                        val translationObj = spliced[index]
                                        instance.proxy.broadcastPacketServer(V1MarkIncompletePacket(translationObj.playerUUID, translationObj.fromLang, translationObj.toLang, translationObj.index, false))
                                        translationObj.job.complete(string)
                                    }
                                }
                            }

                        for (translation in queueLater) {
                            if (queuedTranslations.any { it.id == translation.id && it.queueTime > translation.queueTime } || translation.attempts > 3)
                                continue

                            queuedTranslations.add(translation)
                        }
                    }
                } catch (e: Throwable) {
                    UnityTranslate.logger.error("An error occurred while translating!")
                    e.printStackTrace()
                }
            }
        }, 0L, (instance.config.common.batchTranslateInterval * 1000.0).toLong())

        instances.clear()
        instances.addAll(list)

        UnityTranslate.logger.info("UnityTranslate translation config successfully loaded!")
    }

    companion object {
        const val MAX_TRANSLATION_BATCH = 15
        const val MAX_TRANSLATION_TASKS = 30
    }
}