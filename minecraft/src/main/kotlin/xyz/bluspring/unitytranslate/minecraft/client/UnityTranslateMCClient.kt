package xyz.bluspring.unitytranslate.minecraft.client

import com.mojang.blaze3d.vertex.PoseStack
import gg.essential.universal.utils.toFormattedString
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.UnityTranslate.Companion.logger
import xyz.bluspring.unitytranslate.common.holders.PlayerHolder
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1SetLanguagePreferencesPacket
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.common.util.nativeaccess.CudaHelper
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.compat.talk_balloons.TalkBalloonsCompat
import xyz.bluspring.unitytranslate.common.events.TranscriptEvents
import xyz.bluspring.unitytranslate.minecraft.network.UTClientNetworkSender
import java.util.*
import java.util.function.BiConsumer

class UnityTranslateMCClient {
    val configFile = MinecraftProxy.getConfigPath().resolve("unitytranslate-client.json").toFile()

    init {
        instance = this

        loadConfig()

        if (UnityTranslate.instance.proxy.isLoaded("talk_balloons")) {
            TalkBalloonsCompat.init()
        }
    }

    fun onClientStarted() {
        updateConfig()
    }

    fun onClientStopping() {
        transcriber.stop()
    }

    fun onClientPlayerJoin(player: AbstractClientPlayer) {
        Minecraft.getInstance().execute {
            val languages = EnumSet.copyOf(transcriptHolders.keys)

            if (transcriptHolders.isNotEmpty()) {
                UnityTranslate.instance.proxy.sendPacketClient(V1SetLanguagePreferencesPacket(languages))
            }
        }
    }

    fun onRenderHud(poseStack: PoseStack, delta: Float) {
        transcriptRenderer.render(poseStack, delta)
    }

    fun onClientEndTick() {
        val mc = Minecraft.getInstance()

        if (OPEN_CONFIG_GUI.consumeClick()) {
            mc.setScreen(UTConfigScreen(mc.screen))
        }

        if (CONFIGURE_BOXES.consumeClick()) {
            mc.setScreen(EditTranscriptBoxesScreen(mc.screen))
        }

        if (TOGGLE_TRANSCRIPTION.consumeClick()) {
            isMuted = !isMuted
        }

        if (TOGGLE_BOXES.consumeClick()) {
            shouldRenderBoxes = !shouldRenderBoxes
        }

        if (SET_SPOKEN_LANGUAGE.consumeClick()) {
            mc.setScreen(LanguageSelectScreen(mc.screen, {
                clientConfig.spokenLanguage = it!!
                saveConfig()
                updateConfig()
            }))
        }

        if (CLEAR_TRANSCRIPTS.consumeClick()) {
            for ((_, holder) in transcriptHolders) {
                holder.transcripts.clear()
            }
        }

        for ((index, key) in LANGUAGE_PROFILE_KEYS.withIndex()) {
            if (key.consumeClick()) {
                val language = clientConfig.languageProfiles.getOrNull(index) ?: continue

                clientConfig.spokenLanguage = language
                saveConfig()
                updateConfig()

                displayMessage(MinecraftProxy.literal("Spoken language set to ${language.translationKey}"))
            }
        }

        for ((_, holder) in transcriptHolders) {
            holder.tick()
        }
    }

    companion object {
        lateinit var instance: UnityTranslateMCClient

        var shouldRenderBoxes = true
        var serverHasTranslations = false

        val CONFIGURE_BOXES = KeybindHelper.register(KeyMapping("unitytranslate.configure_boxes", -1, "UnityTranslate"))
        val TOGGLE_TRANSCRIPTION = KeybindHelper.register(KeyMapping("unitytranslate.toggle_transcription", -1, "UnityTranslate"))
        val TOGGLE_BOXES = KeybindHelper.register(KeyMapping("unitytranslate.toggle_boxes", -1, "UnityTranslate"))
        val SET_SPOKEN_LANGUAGE = KeybindHelper.register(KeyMapping("unitytranslate.set_spoken_language", -1, "UnityTranslate"))
        val CLEAR_TRANSCRIPTS = KeybindHelper.register(KeyMapping("unitytranslate.clear_transcripts", -1, "UnityTranslate"))
        //val TRANSLATE_SIGN = KeybindHelper.register(KeyMapping("unitytranslate.translate_sign", GLFW.GLFW_KEY_F8, "UnityTranslate"))
        val OPEN_CONFIG_GUI = KeybindHelper.register(KeyMapping("unitytranslate.open_config", GLFW.GLFW_KEY_F7, "UnityTranslate"))

        val LANGUAGE_PROFILE_KEYS = (0 until 5).map {
            KeybindHelper.register(KeyMapping("unitytranslate.language_profile.${it + 1}", -1, "UnityTranslate"))
        }

        fun allowsClientTranslation(): Boolean {
            val status = clientConfig.clientTranslation

            return if (status == UnityTranslateClientConfig.ClientTranslation.ENABLED)
                true
            else if (status == UnityTranslateClientConfig.ClientTranslation.ENABLED_IF_CUDA)
                UnityTranslate.instance.config.common.shouldUseCuda && CudaHelper.isCudaSupported
            else
                false
        }

        fun displayMessage(text: Component, isError: Boolean = false) {
            if (Minecraft.getInstance().gui == null || Minecraft.getInstance().player == null) {
                if (isError)
                    logger.error("UnityTranslate: ${text.string}")
                else
                    logger.info("UnityTranslate: ${text.string}")

                return
            }

            val full = MinecraftProxy.literal("").copy()
                .append(MinecraftProxy.literal("[UnityTranslate]: ")
                    .copy().withStyle(if (isError) ChatFormatting.RED else ChatFormatting.YELLOW, ChatFormatting.BOLD)
                )
                .append(text)

            Minecraft.getInstance().gui.chat.addMessage(full)
        }
    }
}