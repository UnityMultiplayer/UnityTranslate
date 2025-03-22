package xyz.bluspring.unitytranslate.minecraft.client

import com.mojang.blaze3d.vertex.PoseStack
import dev.architectury.event.events.client.ClientGuiEvent
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.client.ClientTickEvent
import dev.architectury.registry.ReloadListenerRegistry
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.UnityTranslate.Companion.json
import xyz.bluspring.unitytranslate.common.UnityTranslate.Companion.logger
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.common.util.nativeaccess.CudaHelper
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.gui.EditTranscriptBoxesScreen
import xyz.bluspring.unitytranslate.minecraft.client.gui.LanguageSelectScreen
import xyz.bluspring.unitytranslate.minecraft.client.gui.TranscriptBoxRenderer
import xyz.bluspring.unitytranslate.minecraft.client.gui.UTConfigScreen
import xyz.bluspring.unitytranslate.minecraft.client.resources.UTResourceReloadListener
import xyz.bluspring.unitytranslate.minecraft.events.TranscriptEvents
import xyz.bluspring.unitytranslate.minecraft.network.UTClientNetworkSender
import java.util.function.BiConsumer

class UnityTranslateMCClient {
    val configFile = MinecraftProxy.getConfigPath().resolve("unitytranslate-client.json").toFile()

    init {
        instance = this

        loadConfig()
        ClientLifecycleEvent.CLIENT_STARTED.register {
            updateConfig()
        }
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, UTResourceReloadListener())

        ClientGuiEvent.RENDER_HUD.register { poseStack, delta ->
            transcriptRenderer.render(poseStack)
        }

        ClientTickEvent.CLIENT_POST.register { mc ->
            if (OPEN_CONFIG_GUI.consumeClick()) {
                mc.setScreen(UTConfigScreen(mc.screen))
            }

            if (CONFIGURE_BOXES.consumeClick()) {
                mc.setScreen(EditTranscriptBoxesScreen(clientConfig.transcriptBoxes, mc.screen))
            }

            if (TOGGLE_TRANSCRIPTION.consumeClick()) {
                isMuted = !isMuted
            }

            if (TOGGLE_BOXES.consumeClick()) {
                shouldRenderBoxes = !shouldRenderBoxes
            }

            if (SET_SPOKEN_LANGUAGE.consumeClick()) {
                mc.setScreen(LanguageSelectScreen(mc.screen, false))
            }

            if (CLEAR_TRANSCRIPTS.consumeClick()) {
                // TODO: impl
            }

            for ((_, holder) in transcriptHolders) {
                holder.tick()
            }
        }
    }

    fun saveConfig() {
        try {
            if (!this.configFile.parentFile.exists())
                this.configFile.parentFile.mkdirs()

            if (!this.configFile.exists())
                this.configFile.createNewFile()

            val serialized = json.encodeToString(
                UnityTranslateClientConfig.serializer(),
                clientConfig
            )

            this.configFile.writeText(serialized)
        } catch (e: Exception) {
            logger.error("Failed to save UnityTranslate config!")
            e.printStackTrace()
        }

        UnityTranslate.instance.saveConfig()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadConfig() {
        if (!configFile.exists()) {
            clientConfig = UnityTranslateClientConfig()
            return
        }

        try {
            clientConfig = json.decodeFromStream(UnityTranslateClientConfig.serializer(), configFile.inputStream())
        } catch (e: Exception) {
            logger.error("Failed to load UnityTranslate config, reverting to defaults.")
            clientConfig = UnityTranslateClientConfig()
            e.printStackTrace()
        }
    }

    fun updateConfig() {
        transcriber = clientConfig.transcriber.creator.invoke(UnityTranslate.instance, clientConfig.spokenLanguage)
        setupTranscriber(transcriber)

        if (useClientTranslations && !allowsClientTranslation()) {
            useClientTranslations = false
        }

        transcriptHolders.filter { clientConfig.transcriptBoxes.none { b -> b.language == it.key } }
            .forEach { (language, _) ->
                transcriptHolders.remove(language)
            }

        for (config in clientConfig.transcriptBoxes) {
            if (!transcriptHolders.contains(config.language))
                transcriptHolders[config.language] = TranscriptHolder(config.language)
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
                TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, player.uuid, player, text, transcriber.language, updateTime, false), transcriber.language)
            }
        }
    }

    companion object {
        lateinit var instance: UnityTranslateMCClient
        lateinit var transcriber: SpeechTranscriber
        lateinit var clientConfig: UnityTranslateClientConfig

        var isMuted = false
        var useClientTranslations = false
        var shouldRenderBoxes = true

        val transcriptRenderer = TranscriptBoxRenderer()
        val transcriptHolders = mutableMapOf<Language, TranscriptHolder>()

        val CONFIGURE_BOXES = KeybindHelper.register(KeyMapping("unitytranslate.configure_boxes", -1, "UnityTranslate"))
        val TOGGLE_TRANSCRIPTION = KeybindHelper.register(KeyMapping("unitytranslate.toggle_transcription", -1, "UnityTranslate"))
        val TOGGLE_BOXES = KeybindHelper.register(KeyMapping("unitytranslate.toggle_boxes", -1, "UnityTranslate"))
        val SET_SPOKEN_LANGUAGE = KeybindHelper.register(KeyMapping("unitytranslate.set_spoken_language", -1, "UnityTranslate"))
        val CLEAR_TRANSCRIPTS = KeybindHelper.register(KeyMapping("unitytranslate.clear_transcripts", -1, "UnityTranslate"))
        //val TRANSLATE_SIGN = KeybindHelper.register(KeyMapping("unitytranslate.translate_sign", GLFW.GLFW_KEY_F8, "UnityTranslate"))
        val OPEN_CONFIG_GUI = KeybindHelper.register(KeyMapping("unitytranslate.open_config", GLFW.GLFW_KEY_F7, "UnityTranslate"))

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

        fun renderCreditText(poseStack: PoseStack) {
            val version = UnityTranslate.instance.proxy.modVersion
            val font = Minecraft.getInstance().font

            Screen.drawString(poseStack, font, "UnityTranslate v$version", 2, Minecraft.getInstance().window.guiScaledHeight - (font.lineHeight * 2) - 4, 16777215)
            Screen.drawString(poseStack, font, MinecraftProxy.translatable("unitytranslate.credit.author"), 2, Minecraft.getInstance().window.guiScaledHeight - font.lineHeight - 2, 16777215)
        }
    }
}