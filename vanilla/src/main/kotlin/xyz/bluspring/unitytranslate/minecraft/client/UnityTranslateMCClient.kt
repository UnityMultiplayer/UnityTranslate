package xyz.bluspring.unitytranslate.minecraft.client

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.common.util.nativeaccess.CudaHelper
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.events.TranscriptEvents
import xyz.bluspring.unitytranslate.minecraft.network.UTClientNetworkSender
import java.util.function.BiConsumer

class UnityTranslateMCClient {
    init {
        updateConfig()
    }

    fun updateConfig() {
        val clientConfig = UnityTranslate.instance.config.client
        transcriber = clientConfig.transcriber.creator.invoke(UnityTranslate.instance, clientConfig.language)
        setupTranscriber(transcriber)

        if (useClientTranslations && !allowsClientTranslation()) {
            useClientTranslations = false
        }
    }

    fun setupTranscriber(transcriber: SpeechTranscriber) {
        transcriber.updater = BiConsumer { index, text ->
            if (isMuted)
                return@BiConsumer

            val player = Minecraft.getInstance().player ?: return@BiConsumer
            val updateTime = System.currentTimeMillis()

            if (UnityTranslate.instance.proxy.serverSupportsTranslations()) {
                UTClientNetworkSender.sendTranscriptToServer(transcriber.language, text, index, updateTime)
            } else {
                val translatorManager = UnityTranslate.instance.translatorManager

                for ((language, holder) in transcriptHolders) {
                    translatorManager.scope.launch {
                        val translated = translatorManager.queueTranslation(text, transcriber.language, language, player.uuid, index)
                        holder.updateTranscript(player, translated ?: text, transcriber.language, index, updateTime, translated == null)
                    }
                }
            }

            val holder = transcriptHolders[transcriber.language]
            holder?.updateTranscript(player, text, transcriber.language, index, updateTime, false)

            if (holder == null) {
                TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, player.uuid, text, transcriber.language, updateTime, false), transcriber.language)
            }
        }
    }

    companion object {
        lateinit var transcriber: SpeechTranscriber

        var isMuted = false
        var useClientTranslations = false

        val transcriptHolders = mutableMapOf<Language, TranscriptHolder>()

        val CONFIGURE_BOXES = KeybindHelper.register(KeyMapping("unitytranslate.configure_boxes", -1, "UnityTranslate"))
        val TOGGLE_TRANSCRIPTION = KeybindHelper.register(KeyMapping("unitytranslate.toggle_transcription", -1, "UnityTranslate"))
        val TOGGLE_BOXES = KeybindHelper.register(KeyMapping("unitytranslate.toggle_boxes", -1, "UnityTranslate"))
        val SET_SPOKEN_LANGUAGE = KeybindHelper.register(KeyMapping("unitytranslate.set_spoken_language", -1, "UnityTranslate"))
        val CLEAR_TRANSCRIPTS = KeybindHelper.register(KeyMapping("unitytranslate.clear_transcripts", -1, "UnityTranslate"))
        //val TRANSLATE_SIGN = KeybindHelper.register(KeyMapping("unitytranslate.translate_sign", GLFW.GLFW_KEY_F8, "UnityTranslate"))
        val OPEN_CONFIG_GUI = KeybindHelper.register(KeyMapping("unitytranslate.open_config", GLFW.GLFW_KEY_F7, "UnityTranslate"))

        fun allowsClientTranslation(): Boolean {
            val clientConfig = UnityTranslate.instance.config.client
            val status = clientConfig.clientTranslation

            return if (status == UnityTranslateConfig.ClientTranslation.ENABLED)
                true
            else if (status == UnityTranslateConfig.ClientTranslation.ENABLED_IF_CUDA)
                UnityTranslate.instance.config.common.shouldUseCuda && CudaHelper.isCudaSupported
            else
                false
        }

        fun displayMessage(text: Component, isError: Boolean = false) {
            val full = MinecraftProxy.literal("").copy()
                .append(MinecraftProxy.literal("[UnityTranslate]: ")
                    .copy().withStyle(if (isError) ChatFormatting.RED else ChatFormatting.YELLOW, ChatFormatting.BOLD)
                )
                .append(text)

            Minecraft.getInstance().gui.chat.addMessage(full)
        }

        fun renderCreditText(poseStack: PoseStack) {
            val version = UnityTranslate.instance.proxy.modVersion
            val font = Minecraft.getInstance().font

            font.draw(poseStack, "UnityTranslate v$version", 2f, Minecraft.getInstance().window.guiScaledHeight - (font.lineHeight * 2) - 4f, 16777215)
            font.draw(poseStack, MinecraftProxy.translatable("unitytranslate.credit.author"), 2f, Minecraft.getInstance().window.guiScaledHeight - font.lineHeight - 2f, 16777215)
        }
    }
}