package xyz.bluspring.unitytranslate.translator

//#if MC >= 1.20.6
//$$ import xyz.bluspring.unitytranslate.network.payloads.MarkIncompletePayload
//#endif
import dev.architectury.event.events.common.LifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.network.PacketIds
import xyz.bluspring.unitytranslate.util.ClassLoaderProviderForkJoinWorkerThreadFactory
import xyz.bluspring.unitytranslate.util.nativeaccess.CudaState
import xyz.bluspring.unitytranslate.util.nativeaccess.NativeAccess
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ForkJoinPool

object TranslatorManager {
    private var timer: Timer = Timer("UnityTranslate Batch Translate Manager")
    internal val queuedTranslations = ConcurrentLinkedQueue<Translation>()

    private val MULTI_ASTERISK_REGEX = Regex("\\*+")
    private val MULTI_MUSIC_NOTE_REGEX = Regex("[♩♪♫♬♭♮♯°ø\u0602≠≭]+")

    var translationPool = ForkJoinPool((Runtime.getRuntime().availableProcessors() - 3).coerceAtLeast(1))

    var instances = ConcurrentLinkedDeque<LibreTranslateInstance>()
        private set

    fun queueTranslation(line: String, from: Language, to: Language, player: Player, index: Int): CompletableFuture<String> {
        return CompletableFuture<String>().apply {
            val id = "${player.stringUUID}-$index"

            for (previous in queuedTranslations.filter { it.id == id && it.fromLang == from && it.toLang == to }) {
                previous.future.completeExceptionally(Exception("Overridden"))
                queuedTranslations.remove(previous)
            }

            queuedTranslations.add(Translation(
                id,
                line, from, to,
                System.currentTimeMillis(),
                this,
                player, index
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
            val translated = instance.translate(line, from, to)
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

    fun batchTranslateLines(lines: List<String>, from: Language, to: Language): List<String>? {
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

    fun installLibreTranslate() {
        loadFromConfig()

        LocalLibreTranslateInstance.installLibreTranslate().thenApplyAsync {
            try {
                LocalLibreTranslateInstance.launchLibreTranslate(it) { a -> instances.addFirst(a) }
            } catch (e: Throwable) {
                UnityTranslate.logger.error("Failed to launch local LibreTranslate instance!")
                e.printStackTrace()
            }
        }
    }

    fun init() {
        loadFromConfig()

        LifecycleEvent.SERVER_STARTING.register {
            if (UnityTranslate.config.server.shouldRunTranslationServer && LocalLibreTranslateInstance.canRunLibreTranslate()) {
                if (UnityTranslate.instance.proxy.isClient() && !LocalLibreTranslateInstance.isLibreTranslateInstalled()) {
                    UnityTranslateClient.openDownloadRequest()
                } else {
                    installLibreTranslate()
                }
            }
        }

        LifecycleEvent.SERVER_STOPPING.register {
            timer.cancel()
            instances.removeIf { it is LocalLibreTranslateInstance }
        }

        PlayerEvent.PLAYER_QUIT.register { player ->
            queuedTranslations.removeIf { it.player.uuid == player.uuid }
        }
    }

    fun loadFromConfig() {
        // Forge shenanigans
        val customThreadPool = ForkJoinPool(1, ClassLoaderProviderForkJoinWorkerThreadFactory(Thread.currentThread().contextClassLoader), null, false)
        customThreadPool.execute {
            loadFromConfigBlocking()
            customThreadPool.shutdown()
        }
    }

    fun loadFromConfigBlocking() {
        UnityTranslate.logger.info("Loading UnityTranslate translation configs...")

        val list = mutableListOf<LibreTranslateInstance>()

        timer.cancel()
        timer = Timer("UnityTranslate Batch Translate Manager")
        
        translationPool.shutdownNow()
        translationPool = ForkJoinPool((Runtime.getRuntime().availableProcessors() - 3).coerceAtLeast(1), ClassLoaderProviderForkJoinWorkerThreadFactory(Thread.currentThread().contextClassLoader), null, false)

        for (server in UnityTranslate.config.server.offloadServers) {
            try {
                val instance = LibreTranslateInstance(server.url, server.weight, server.authKey)
                list.add(instance)
            } catch (e: Exception) {
                UnityTranslate.logger.error("Failed to load an offloaded server instance!")
                e.printStackTrace()
            }
        }

        if (LocalLibreTranslateInstance.currentInstance != null) {
            list.add(0, LocalLibreTranslateInstance.currentInstance!!)
        }

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    val queueLater = ConcurrentLinkedQueue<Translation>()
                    val toTranslate = mutableMapOf<Pair<Language, Language>, MutableList<Translation>>()

                    while (queuedTranslations.isNotEmpty()) {
                        val translation = queuedTranslations.remove()

                        toTranslate.computeIfAbsent(translation.fromLang to translation.toLang) { mutableListOf() }
                            .add(translation)
                    }

                    toTranslate.forEach { (from, to), allTranslations ->
                        val translations = allTranslations.filter {
                            if (it.player is ServerPlayer)
                                !it.player.hasDisconnected()
                            else true
                        }

                        translations.chunked(LibreTranslateInstance.MAX_CONCURRENT_TRANSLATIONS)
                            .forEach { spliced ->
                                CompletableFuture.supplyAsync({
                                    batchTranslateLines(spliced.map { it.text }, from, to)
                                }, translationPool)
                                    .whenCompleteAsync({ t, u ->
                                        spliced.forEachIndexed { i, translation ->
                                            if (t != null && u == null) {
                                                broadcastIncomplete(false, translation)
                                                translation.future.completeAsync { t[i] }
                                            } else {
                                                if (translation.player is ServerPlayer) {
                                                    broadcastIncomplete(true, translation)
                                                }

                                                translation.attempts++
                                                queueLater.add(translation)
                                            }
                                        }
                                    }, translationPool)
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

    //#if MC <= 1.20.4
    // turns out, Forge requires us to rebuild the buffer every time we send it to a player,
    // so unfortunately, we cannot reuse the buffer.
    private fun buildBroadcastPacket(isIncomplete: Boolean, translation: Translation): FriendlyByteBuf {
        val buf = UnityTranslate.instance.proxy.createByteBuf()
        buf.writeEnum(translation.fromLang)
        buf.writeEnum(translation.toLang)
        buf.writeUUID(translation.player.uuid)
        buf.writeVarInt(translation.index)
        buf.writeBoolean(isIncomplete)

        return buf
    }
    //#endif

    private fun broadcastIncomplete(isIncomplete: Boolean, translation: Translation) {
        if (translation.player !is ServerPlayer)
            return

        val source = translation.player

        if (UnityTranslate.hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                //#if MC >= 1.20.6
                //$$  UnityTranslate.instance.proxy.sendPacketServer(player, MarkIncompletePayload(translation.fromLang, translation.toLang, translation.player.uuid, translation.index, isIncomplete))
                //#else
                val buf = buildBroadcastPacket(isIncomplete, translation)
                UnityTranslate.instance.proxy.sendPacketServer(player, PacketIds.MARK_INCOMPLETE, buf)
                //#endif
            }
        } else {
            //#if MC >= 1.20.6
            //$$  UnityTranslate.instance.proxy.sendPacketServer(source, MarkIncompletePayload(translation.fromLang, translation.toLang, translation.player.uuid, translation.index, isIncomplete))
            //#else
            val buf = buildBroadcastPacket(isIncomplete, translation)
            UnityTranslate.instance.proxy.sendPacketServer(source, PacketIds.MARK_INCOMPLETE, buf)
            //#endif
        }
    }
}