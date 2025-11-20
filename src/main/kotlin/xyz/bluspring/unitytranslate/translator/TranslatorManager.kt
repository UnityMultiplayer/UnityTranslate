package xyz.bluspring.unitytranslate.translator

//? if >= 1.20.6 {
/*import xyz.bluspring.unitytranslate.network.payloads.MarkIncompletePayload
*///?}
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import xyz.bluspring.modernnetworking.api.minecraft.VanillaPacketSender
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.library.util.collect
import xyz.bluspring.unitytranslate.library.util.concurrent
import xyz.bluspring.unitytranslate.network.payloads.MarkIncompletePayload
import xyz.bluspring.unitytranslate.translator.library.UnityTranslateLibInstance
import xyz.bluspring.unitytranslate.util.nativeaccess.CudaState
import xyz.bluspring.unitytranslate.util.nativeaccess.NativeAccess
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

object TranslatorManager {
    private var timer: Timer = Timer("UnityTranslate Batch Translate Manager")
    internal val queuedTranslations = ConcurrentLinkedQueue<Translation>()

    private val MULTI_ASTERISK_REGEX = Regex("\\*+")
    private val MULTI_MUSIC_NOTE_REGEX = Regex("[♩♪♫♬♭♮♯°ø\u0602≠≭]+")

    private val cachedTranslations = Collections.synchronizedMap(mutableMapOf<Pair<Language, Language>, Cache<String, String>>())

    val scope = CoroutineScope(Dispatchers.IO)

    var instances = ConcurrentLinkedDeque<LibreTranslateInstance>()
        private set

    private fun getCache(from: Language, to: Language): Cache<String, String> {
        return this.cachedTranslations.computeIfAbsent(from to to) {
            CacheBuilder.newBuilder()
                .maximumSize(20_000)
                .expireAfterAccess(3.minutes.toJavaDuration())
                .concurrencyLevel(3)
                .build()
        }
    }

    fun queueTranslation(line: String, from: Language, to: Language, player: Player, index: Int, chunk: Int): CompletableDeferred<String> {
        return CompletableDeferred<String>().apply {
            val cached = getCache(from, to).getIfPresent(line)

            if (cached != null) {
                this.complete(cached)
                return@apply
            }

            val id = "${player.stringUUID}-$index-$chunk"

            for (previous in queuedTranslations.filter { it.id == id && it.fromLang == from && it.toLang == to }) {
                previous.future.cancel("Overridden")
                queuedTranslations.remove(previous)
            }

            queuedTranslations.add(Translation(
                id,
                line, from, to,
                System.currentTimeMillis(),
                this,
                player, index, chunk
            ))
        }
    }

    fun detectLanguage(line: String): Language? {
        val possible = instances.sortedByDescending { it.weight.asInt() }

        if (possible.isEmpty()) {
            UnityTranslate.logger.warn("No available instances available for detecting language for line \"$line\"!")
            return null
        }

        for (instance in possible) {
            val lang = instance.detectLanguage(line) ?: continue
            return lang
        }

        UnityTranslate.logger.warn("Failed to detect language for line \"$line\"!")

        return null
    }

    fun translateLine(line: String, from: Language, to: Language): String? {
        val possible = instances.filter { it.supportsLanguage(from, to) }.sortedByDescending { it.weight.asInt() }

        if (possible.isEmpty()) {
            UnityTranslate.logger.warn("No instances available for translating $from to $to!)")
            return line
        }

        var index = 0

        for (instance in possible) {
            if (instance.currentlyTranslating >= LibreTranslateInstance.MAX_CONCURRENT_TRANSLATIONS && index++ < possible.size - 1)
                continue

            instance.currentlyTranslating++
            val translated = runBlocking {
                instance.translate(line, from, to)
            }
            instance.currentlyTranslating--

            if (translated == null) {
                continue
            }

            return translated.replace(MULTI_ASTERISK_REGEX, "**")
                .replace(MULTI_MUSIC_NOTE_REGEX, "")
        }

        UnityTranslate.logger.warn("Failed to translate $line from $from to $to!")

        return null
    }

    suspend fun batchTranslateLines(lines: List<String>, from: Language, to: Language): List<String>? {
        val possible = instances.filter { it.supportsLanguage(from, to) }.sortedByDescending { it.weight.asInt() }

        if (possible.isEmpty()) {
            UnityTranslate.logger.warn("No instances available for translating $from to $to!)")
            return lines
        }

        var index = 0

        for (instance in possible) {
            if (instance.currentlyTranslating >= LibreTranslateInstance.MAX_CONCURRENT_TRANSLATIONS && index++ < possible.size - 1)
                continue

            instance.currentlyTranslating += lines.size
            val translated = instance.batchTranslate(lines, from, to)
            instance.currentlyTranslating -= lines.size

            if (translated == null) {
                continue
            }

            return translated.map {
                it.replace(MULTI_ASTERISK_REGEX, "**")
                    .replace(MULTI_MUSIC_NOTE_REGEX, "")
            }
        }

        UnityTranslate.logger.warn("Failed to translate lines from $from to $to:")
        for (line in lines) {
            UnityTranslate.logger.warn(" - $line")
        }

        return null
    }

    var hasChecked = false

    fun checkSupportsCuda(): Boolean {
        if (!UnityTranslate.config.server.shouldUseCuda) {
            UnityTranslate.logger.info("CUDA is disabled in the config, not enabling CUDA support.")
            return false
        }

        return NativeAccess.isCudaSupported().apply {
            if (!hasChecked) {
                if (this == CudaState.AVAILABLE)
                    UnityTranslate.logger.info("CUDA is supported, using GPU for translations.")
                else {
                    UnityTranslate.logger.info("CUDA is not supported, using CPU for translations.")
                    UnityTranslate.logger.info("CUDA state: $ordinal ($name): $message")
                }

                hasChecked = true
            }
        } == CudaState.AVAILABLE
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun installLibreTranslate() {
        loadFromConfig()

        GlobalScope.launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            try {
                UnityTranslateLibInstance.tryLoad()
                if (UnityTranslateLibInstance.isLibraryLoaded)
                    instances.addFirst(UnityTranslateLibInstance.instance)
            } catch (e: Throwable) {
                UnityTranslate.logger.error("Failed to launch UnityTranslateLib instance!")
                e.printStackTrace()
            }
        }
    }

    fun init() {
        loadFromConfig()
    }

    fun serverStarting(server: MinecraftServer) {
        if (server::class.java.name.endsWith("ReplayServer"))
            return

        if (UnityTranslate.config.server.shouldRunTranslationServer) {
            installLibreTranslate()
        }
    }

    fun serverStopping() {
        timer.cancel()
        instances.removeIf { it is UnityTranslateLibInstance }
    }

    fun playerQuit(player: Player) {
        queuedTranslations.removeIf { it.player.uuid == player.uuid }
    }

    fun loadFromConfig() {
        loadFromConfigBlocking()
    }

    fun loadFromConfigBlocking() {
        UnityTranslate.logger.info("Loading UnityTranslate translation configs...")

        val list = mutableListOf<LibreTranslateInstance>()

        timer.cancel()
        timer = Timer("UnityTranslate Batch Translate Manager")

        for (server in UnityTranslate.config.server.offloadServers) {
            try {
                val instance = LibreTranslateInstance(server.url, server.weight, server.authKey)
                list.add(instance)
            } catch (e: Exception) {
                UnityTranslate.logger.error("Failed to load an offloaded server instance!")
                e.printStackTrace()
            }
        }

        if (UnityTranslateLibInstance.isLibraryLoaded) {
            list.add(0, UnityTranslateLibInstance.instance)
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    val queueLater = ConcurrentLinkedQueue<Translation>()
                    val toTranslate = mutableMapOf<Pair<Language, Language>, MutableList<Translation>>()

                    while (queuedTranslations.isNotEmpty()) {
                        val translation = queuedTranslations.remove()

                        if (translation.future.isCancelled || translation.future.isCompleted)
                            continue

                        toTranslate.computeIfAbsent(translation.fromLang to translation.toLang) { mutableListOf() }
                            .add(translation)
                    }

                    toTranslate.forEach { (from, to), allTranslations ->
                        val translations = allTranslations.filter {
                            if (it.player is ServerPlayer)
                                !it.player.hasDisconnected()
                            else true
                        }

                        scope.launch(start = CoroutineStart.UNDISPATCHED) {
                            translations.chunked(LibreTranslateInstance.MAX_CONCURRENT_TRANSLATIONS)
                                .asFlow()
                                .concurrent()
                                .collect { spliced ->
                                    try {
                                        val translated = batchTranslateLines(spliced.map { it.text }, from, to)
                                        if (translated == null) {
                                            throw IllegalStateException("Failed to translate lines! $spliced")
                                        }

                                        for ((index, translation) in spliced.withIndex()) {
                                            broadcastIncomplete(false, translation)
                                            translation.future.complete(translated[index].apply {
                                                getCache(translation.fromLang, translation.toLang).put(translation.text, translated[index])
                                            })
                                        }
                                    } catch (_: Throwable) {
                                        for (translation in spliced) {
                                            if (translation.player is ServerPlayer) {
                                                broadcastIncomplete(true, translation)
                                            }

                                            translation.attempts++
                                            queueLater.add(translation)
                                        }
                                    }
                                }
                        }
                    }

                    for (translation in queueLater) {
                        if (queuedTranslations.any { it.id == translation.id && it.queueTime > translation.queueTime } || translation.attempts > 3)
                            continue

                        queuedTranslations.add(translation)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0L, (UnityTranslate.config.server.batchTranslateInterval * 1000.0).toLong())

        instances = ConcurrentLinkedDeque(list)

        UnityTranslate.logger.info("UnityTranslate translation config successfully loaded!")
    }

    private fun broadcastIncomplete(isIncomplete: Boolean, translation: Translation) {
        if (translation.player !is ServerPlayer)
            return

        val source = translation.player
        val payload = MarkIncompletePayload(translation.fromLang, translation.toLang, translation.player.uuid, translation.index, isIncomplete)

        if (UnityTranslate.hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                VanillaPacketSender.sendToPlayer(player, payload)
            }
        } else {
            VanillaPacketSender.sendToPlayer(source, payload)
        }
    }
}