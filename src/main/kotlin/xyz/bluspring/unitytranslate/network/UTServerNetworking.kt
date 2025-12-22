package xyz.bluspring.unitytranslate.network

 import kotlinx.atomicfu.locks.synchronized
 import kotlinx.coroutines.CompletableDeferred
 import kotlinx.coroutines.CoroutineStart
 import kotlinx.coroutines.launch
 import net.minecraft.ChatFormatting
 import net.minecraft.network.chat.Component
 import net.minecraft.server.level.ServerLevel
 import net.minecraft.server.level.ServerPlayer
 //? if >= 1.21.11 {
 /*import net.minecraft.server.permissions.Permissions
 *///?} else {
 import net.minecraft.commands.Commands
 //?}
 import net.minecraft.world.entity.player.Player
 import xyz.bluspring.modernnetworking.api.minecraft.VanillaPacketSender
 import xyz.bluspring.unitytranslate.Language
 import xyz.bluspring.unitytranslate.UnityTranslate
 import xyz.bluspring.unitytranslate.UnityTranslate.Companion.hasVoiceChat
 import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
 import xyz.bluspring.unitytranslate.network.payloads.SendTranscriptToClientPayload
 import xyz.bluspring.unitytranslate.network.payloads.ServerSupportPayload
 import xyz.bluspring.unitytranslate.translator.TranslatorManager
 import xyz.bluspring.unitytranslate.translator.library.TranslationModelDownloadQueue
 import xyz.bluspring.unitytranslate.translator.library.UnityTranslateLibInstance
 import xyz.bluspring.unitytranslate.util.multiversion.*
 import java.util.*
 import java.util.concurrent.ConcurrentHashMap
 import java.util.concurrent.ConcurrentLinkedDeque

object UTServerNetworking {
    val proxy = UnityTranslate.instance.proxy
    val playerLanguages = Collections.synchronizedMap<UUID, Language>(mutableMapOf())
    val usedLanguages = Collections.synchronizedMap<UUID, EnumSet<Language>>(mutableMapOf())

    val registry = PacketDefinitions.registry

    fun init() {
        PacketDefinitions.init()

        registry.addServerboundHandler(PacketDefinitions.SET_USED_LANGUAGES) { packet, ctx ->
            val languages = EnumSet.copyOf(packet.languages)
            usedLanguages[ctx.player.uuid] = languages

            queueLanguagesToDownload()
        }

        registry.addServerboundHandler(PacketDefinitions.SEND_TRANSCRIPT_TO_SERVER) { packet, ctx ->
            val sourceLanguage = packet.sourceLanguage
            val text = packet.text
            val index = packet.index
            val updateTime = packet.updateTime

            if (!canPlayerRequestTranslations(ctx.player))
                return@addServerboundHandler

            val translations = ConcurrentHashMap<Language, String>()
            val translationsToSend = ConcurrentLinkedDeque<Language>()

            val segments = mutableListOf<String>()
            var currentString = ""

            for (string in text.split(" ")) {
                currentString += "$string "

                if (currentString.length > 256) {
                    segments.add(currentString.trim())
                    currentString = ""
                }
            }

            if (!currentString.isEmpty()) {
                segments.add(currentString.trim())
            }

            Language.entries.filter { usedLanguages.values.any { b -> b.contains(it) } }.map {
                Pair(it, if (sourceLanguage == it)
                    segments.map { segment -> CompletableDeferred(segment) }
                else
                    segments.mapIndexed { i, segment -> TranslatorManager.queueTranslation(segment, sourceLanguage, it, ctx.player, index, i) }
                )
            }
                .forEach { (language, futures) ->
                    TranslatorManager.scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        val translated = futures.map { future -> future.await() }.joinToString(" ")
                        translations[language] = translated
                        translationsToSend.add(language)

                        ctx.player.server!!.execute {
                            if (translationsToSend.isNotEmpty()) {
                                broadcastTranslations(ctx.player, sourceLanguage, index, updateTime, translationsToSend, translations)
                                translationsToSend.clear()
                            }
                        }
                    }
                }
        }

        registry.addServerboundHandler(PacketDefinitions.SET_CURRENT_LANGUAGE) { packet, ctx ->
            val language = packet.language
            playerLanguages[ctx.player.uuid] = language
            queueLanguagesToDownload()
        }
    }

    fun onPlayerJoin(player: ServerPlayer) {
        if (UnityTranslateLibInstance.isLibraryLoaded)
            VanillaPacketSender.sendToPlayer(player, ServerSupportPayload)
        else if ((player.level() as ServerLevel).server.isSingleplayer ||
            //? if >= 1.21.11 {
            /*player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)
            *///?} else {
            player.hasPermissions(Commands.LEVEL_ADMINS)
            //?}
        )
            player.displayClientMessage(Component.translatableWithFallback("unitytranslate.error.library_not_loaded", "[UnityTranslate] The UnityTranslate mod is installed on the server, but the translation library could not be loaded! This may be caused by using an unsupported platform, such as macOS, or using an ARM-based CPU architecture.\nIf you believe this to be in error, please report this as an issue with your server's system information!").withStyle(ChatFormatting.RED), false)
    }

    fun onPlayerLeave(player: ServerPlayer) {
        usedLanguages.remove(player.uuid)
    }

    private fun broadcastTranslations(source: ServerPlayer, sourceLanguage: Language, index: Int, updateTime: Long, translationsToSend: ConcurrentLinkedDeque<Language>, translations: ConcurrentHashMap<Language, String>) {
        val toSend = translations.filter { a -> translationsToSend.contains(a.key) }

        if (hasVoiceChat) {
            val nearby = UTVoiceChatCompat.getNearbyPlayers(source)

            for (player in nearby) {
                if (UTVoiceChatCompat.isPlayerDeafened(player) && player != source)
                    continue

                VanillaPacketSender.sendToPlayer(player, SendTranscriptToClientPayload(source.uuid, sourceLanguage, index, updateTime, toSend))
            }
        } else {
            VanillaPacketSender.sendToPlayer(source, SendTranscriptToClientPayload(source.uuid, sourceLanguage, index, updateTime, toSend))
        }
    }

    fun canPlayerRequestTranslations(player: Player): Boolean {
        return UnityTranslate.instance.proxy.hasTranscriptPermission(player)
    }

    fun queueLanguagesToDownload() {
        synchronized(this.playerLanguages) {
            for (spoken in this.playerLanguages.values) {
                synchronized(this.usedLanguages) {
                    for (translated in this.usedLanguages.values.flatten()) {
                        TranslationModelDownloadQueue.queueDownload(spoken, translated)
                    }
                }
            }
        }
    }
}