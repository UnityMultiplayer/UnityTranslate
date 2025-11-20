package xyz.bluspring.unitytranslate.client

//? if >= 1.20.6 {
/*import xyz.bluspring.unitytranslate.network.payloads.SendTranscriptToServerPayload
*///?}
import com.mojang.blaze3d.platform.InputConstants
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import xyz.bluspring.modernnetworking.api.minecraft.VanillaPacketSender
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.gui.*
import xyz.bluspring.unitytranslate.client.transcribers.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.client.transcribers.windows.sapi5.WindowsSpeechApiTranscriber
import xyz.bluspring.unitytranslate.compat.talkballoons.TalkBalloonsCompat
import xyz.bluspring.unitytranslate.network.UTClientNetworking
import xyz.bluspring.unitytranslate.network.payloads.SendTranscriptToServerPayload
import xyz.bluspring.unitytranslate.transcript.TranscriptHolder
import xyz.bluspring.unitytranslate.translator.TranslatorManager
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.BiConsumer
import java.util.function.Consumer

class UnityTranslateClient {
    init {
        WindowsSpeechApiTranscriber.isSupported() // runs a check to load Windows Speech API. why write the code again anyway?
        setupCompat()
        updateConfig()

        transcriber = UnityTranslate.config.client.transcriber.creator.invoke(UnityTranslate.config.client.language)
        setupTranscriber(transcriber)

        UTClientNetworking.init()
    }

    fun clientRenderHud(guiGraphics: GuiGraphics, delta: Float) {
        if (shouldRenderBoxes && UnityTranslate.config.client.enabled && !Minecraft.getInstance().options.hideGui) {
            for (languageBox in languageBoxes) {
                languageBox.render(guiGraphics, delta)
            }
        }
    }

    fun clientStopping() {
//        LocalLibreTranslateInstance.killOpenInstances()
    }

    fun clientTick(mc: Minecraft) {
        if (CONFIGURE_BOXES.consumeClick()) {
            mc.setScreen(EditTranscriptBoxesScreen(languageBoxes))
        }

        if (TOGGLE_TRANSCRIPTION.consumeClick()) {
            shouldTranscribe = !shouldTranscribe
            mc.player?.displayClientMessage(
                Component.translatable("unitytranslate.transcript")
                    .append(": ")
                    .append(if (shouldTranscribe) CommonComponents.OPTION_ON else CommonComponents.OPTION_OFF), true
            )
        }

        if (TOGGLE_BOXES.consumeClick() && mc.screen !is EditTranscriptBoxesScreen) {
            shouldRenderBoxes = !shouldRenderBoxes
            mc.player?.displayClientMessage(
                Component.translatable("unitytranslate.transcript_boxes")
                    .append(": ")
                    .append(if (shouldRenderBoxes) CommonComponents.OPTION_ON else CommonComponents.OPTION_OFF),
                true
            )
        }

        if (SET_SPOKEN_LANGUAGE.consumeClick() && mc.screen == null) {
            mc.setScreen(LanguageSelectScreen(null, LanguageSelectType.SPOKEN))
        }

        if (CLEAR_TRANSCRIPTS.consumeClick()) {
            for (box in transcriptHolders) {
                box.transcripts.clear()
            }
        }

        if (OPEN_CONFIG_GUI.consumeClick()) {
            mc.setScreen(UTConfigScreen(null))
        }

        /*if (TRANSLATE_SIGN.consumeClick()) {
            if (mc.player != null && mc.level != null) {
                val hitResult = mc.player?.pick(7.5, mc.frameTime, false)

                if (hitResult != null && hitResult is BlockHitResult) {
                    val buf = UnityTranslate.instance.proxy.createByteBuf()
                    buf.writeBlockPos(hitResult.blockPos)

                    UnityTranslate.instance.proxy.sendPacketClient(PacketIds.TRANSLATE_SIGN, buf)
                }
            }
        }*/

        // prune transcripts
        val currentTime = System.currentTimeMillis()
        for (box in transcriptHolders) {
            if (box.transcripts.size > 50) {
                for (i in 0..(box.transcripts.size - 50)) {
                    box.transcripts.remove()
                }
            }

            val clientConfig = UnityTranslate.config.client

            if (clientConfig.disappearingText) {
                for (transcript in box.transcripts) {
                    if (currentTime >= (transcript.arrivalTime + (clientConfig.disappearingTextDelay * 1000L).toLong() + (clientConfig.disappearingTextFade * 1000L).toLong())) {
                        box.transcripts.remove(transcript)
                    }
                }
            }
        }
    }

    fun clientJoinWorld() {
        for (consumer in queuedForJoin) {
            consumer.accept(Minecraft.getInstance())
        }

        queuedForJoin.clear()

        UTClientNetworking.onClientJoin()

        if (transcriber is BrowserSpeechTranscriber) {
            (transcriber as BrowserSpeechTranscriber).openWebsite()
        }
    }

    fun clientLeaveWorld() {
        UTClientNetworking.onClientLeave()
    }

    fun setupTranscriber(transcriber: SpeechTranscriber) {
        transcriber.updater = BiConsumer { index, text ->
            if (!shouldTranscribe)
                return@BiConsumer

            val updateTime = System.currentTimeMillis()

            if (connectedServerHasSupport) {
                VanillaPacketSender.sendToServer(SendTranscriptToServerPayload(transcriber.language, text, index, updateTime))

                getTranscriptHolder(transcriber.language)?.updateTranscript(Minecraft.getInstance().player!!, text, transcriber.language, index, updateTime, false)
            } else {
                if (Minecraft.getInstance().player == null)
                    return@BiConsumer

                for (box in transcriptHolders) {
                    if (box.language == transcriber.language) {
                        box.updateTranscript(Minecraft.getInstance().player!!, text, transcriber.language, index, updateTime, false)

                        continue
                    }

                    TranslatorManager.scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        val translated = TranslatorManager.queueTranslation(text, transcriber.language, box.language, Minecraft.getInstance().player!!, index, 0).await()
                        box.updateTranscript(Minecraft.getInstance().player!!, translated, transcriber.language, index, updateTime, false)
                    }
                }
            }
        }
    }

    private fun setupCompat() {
        if (isTalkBalloonsInstalled) {
            TalkBalloonsCompat.init()
        }
    }

    companion object {
        lateinit var transcriber: SpeechTranscriber

        var connectedServerHasSupport = false

        var shouldTranscribe = true
            set(value) {
                field = value
                transcriber.setMuted(!value)
            }

        var shouldRenderBoxes = true

        val transcriptHolders = mutableSetOf<TranscriptHolder>()

        fun getTranscriptHolder(language: Language): TranscriptHolder? {
            return transcriptHolders.firstOrNull { it.language == language }
        }

        val languageBoxes: MutableList<TranscriptBox>
            get() {
                return UnityTranslate.config.client.transcriptBoxes
            }

        val CONFIGURE_BOXES = (KeyMapping("unitytranslate.configure_boxes", -1, "UnityTranslate"))
        val TOGGLE_TRANSCRIPTION = (KeyMapping("unitytranslate.toggle_transcription", -1, "UnityTranslate"))
        val TOGGLE_BOXES = (KeyMapping("unitytranslate.toggle_boxes", -1, "UnityTranslate"))
        val SET_SPOKEN_LANGUAGE = (KeyMapping("unitytranslate.set_spoken_language", -1, "UnityTranslate"))
        val CLEAR_TRANSCRIPTS = (KeyMapping("unitytranslate.clear_transcripts", -1, "UnityTranslate"))
        //val TRANSLATE_SIGN = (KeyMapping("unitytranslate.translate_sign", InputConstants.KEY_F8, "UnityTranslate"))
        val OPEN_CONFIG_GUI = (KeyMapping("unitytranslate.open_config", InputConstants.KEY_F7, "UnityTranslate"))

        @JvmStatic
        val keys: List<KeyMapping> = listOf(CONFIGURE_BOXES, TOGGLE_TRANSCRIPTION, TOGGLE_BOXES, SET_SPOKEN_LANGUAGE, CLEAR_TRANSCRIPTS, OPEN_CONFIG_GUI)

        val clientConfig = UnityTranslate.config.client

        val isTalkBalloonsInstalled = UnityTranslate.instance.proxy.isModLoaded("talk_balloons")

        fun updateConfig() {
            transcriptHolders.removeIf { holder ->
                clientConfig.transcriptBoxes.none { it.language == holder.language }
                        && clientConfig.language != holder.language
                        && clientConfig.balloonLanguage != holder.language
            }

            if (transcriptHolders.none { it.language == clientConfig.language })
                transcriptHolders.add(TranscriptHolder(clientConfig.language))

            if (transcriptHolders.none { it.language == clientConfig.balloonLanguage })
                transcriptHolders.add(TranscriptHolder(clientConfig.balloonLanguage))

            for (box in clientConfig.transcriptBoxes) {
                if (transcriptHolders.none { it.language == box.language })
                    transcriptHolders.add(TranscriptHolder(box.language))
            }
        }

        fun displayMessage(component: Component, isError: Boolean = false) {
            val full = Component.empty()
                .append(Component.literal("[UnityTranslate]: ")
                    .withStyle(if (isError) ChatFormatting.RED else ChatFormatting.YELLOW, ChatFormatting.BOLD)
                )
                .append(component)

            Minecraft.getInstance().gui.chat.addMessage(full)
        }

        fun renderCreditText(guiGraphics: GuiGraphics) {
            val version = UnityTranslate.instance.proxy.modVersion
            val font = Minecraft.getInstance().font

            guiGraphics.drawString(font, "UnityTranslate v$version", 2, Minecraft.getInstance().window.guiScaledHeight - (font.lineHeight * 2) - 4, 0xAAAAAA)
            guiGraphics.drawString(font, Component.translatable("unitytranslate.credit.author"), 2, Minecraft.getInstance().window.guiScaledHeight - font.lineHeight - 2, 0xAAAAAA)
        }

        private val queuedForJoin = ConcurrentLinkedQueue<Consumer<Minecraft>>()
    }
}