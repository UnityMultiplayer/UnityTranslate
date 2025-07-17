package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIConstraints
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.button
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.cycleButton
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.expandableSection
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.slider
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.toggleButton
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.withScrollbar
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ExpandableSection
import xyz.bluspring.unitytranslate.transcriber.whisper.WhisperModel
import java.nio.file.Path
import kotlin.io.path.name

class UTConfigScreen(private val parent: Screen?, copyFrom: UTConfigScreen? = null) : WindowScreen(ElementaVersion.V10) {
    val background = UIBlock(ElementaUIHelpers.BACKGROUND_COLOR).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 100.percentOfWindow
        this.height = 100.percentOfWindow
    } childOf window

    val topText = UIText("UnityTranslate").constrain {
        this.x = CenterConstraint()
        this.y = 12.pixels
    } childOf window

    val sections = ScrollComponent().constrain {
        this.x = CenterConstraint()
        this.y = 24.pixels
        this.width = 90.percentOfWindow
        this.height = 100.percentOfWindow - 24.pixels - 35.pixels
    }.apply {
        fun UIConstraints.defaultButtonConstraints() {
            this.x = CramSiblingConstraint(5f)
            this.width = 40.percentOfWindow
            this.y = CramSiblingConstraint(5f)
        }

        // Client Section
        expandableSection(I18n.get("gui.unitytranslate.config.client")) {
            val config = UnityTranslateMCClient.Companion.clientConfig

            toggleButton("Mod Status", config.enabled) { config.enabled = it }
                .constrain { defaultButtonConstraints() } childOf this

            toggleButton("Mute Transcript when Voice Chat Muted", config.muteTranscriptWhenVoiceChatMuted) { config.muteTranscriptWhenVoiceChatMuted = it }
                .constrain { defaultButtonConstraints() } childOf this

            toggleButton("Dark Mode", config.isDarkMode) {
                config.isDarkMode = it
                ElementaUIHelpers.setup()

                Minecraft.getInstance().setScreen(UTConfigScreen(this@UTConfigScreen.parent, this@UTConfigScreen))
            }
                .constrain { defaultButtonConstraints() } childOf this

            button("Edit Transcript Boxes")
                .constrain { defaultButtonConstraints() }
                .onMouseClick {
                    Minecraft.getInstance().setScreen(EditTranscriptBoxesScreen(this@UTConfigScreen))
                } childOf this

            button("Set Spoken Language (${I18n.get(config.spokenLanguage.translationKey)})")
                .constrain { defaultButtonConstraints() }
                .onMouseClick {
                    Minecraft.getInstance().setScreen(LanguageSelectScreen(this@UTConfigScreen, { language ->
                        config.spokenLanguage = language!!
                    }))
                } childOf this

            button("Set Balloon Language (${I18n.get(if (config.balloonLanguage == null) config.spokenLanguage.translationKey else config.balloonLanguage!!.translationKey)})")
                .constrain { defaultButtonConstraints() }
                .onMouseClick {
                    Minecraft.getInstance().setScreen(LanguageSelectScreen(this@UTConfigScreen, { language ->
                        config.balloonLanguage = language
                    }, true))
                } childOf this

            val browserPath = button("Browser Path: ${if (config.browserPath.isBlank()) "(default browser)" else Path.of(config.browserPath).name}")
                .constrain { defaultButtonConstraints() }

            val whisperModel = cycleButton("Whisper Model", WhisperModel.entries, config.whisperModel) {
                config.whisperModel = it
            }.constrain { defaultButtonConstraints() }

            fun updateTranscriberVisibility() {
                when (config.transcriber) {
                    TranscriberType.SPHINX -> throw IllegalStateException()
                    TranscriberType.BROWSER -> {
                        whisperModel.hide()
                        browserPath.unhide()
                    }
                    TranscriberType.WINDOWS_SAPI -> throw IllegalStateException()
                    TranscriberType.WHISPER -> {
                        browserPath.hide()
                        whisperModel.unhide()
                    }
                }
            }

            cycleButton("Transcriber Type", TranscriberType.entries.filter { it.isAvailable }, config.transcriber) {
                config.transcriber = it
                updateTranscriberVisibility()
            }.constrain { defaultButtonConstraints() } childOf this

            browserPath childOf this
            whisperModel childOf this

            updateTranscriberVisibility()

            cycleButton("Client Translation", UnityTranslateClientConfig.ClientTranslation.entries, config.clientTranslation) {
                config.clientTranslation = it
            }.constrain { defaultButtonConstraints() } childOf this

            val disappearingTextDelay = slider(0.2f, 60f, config.disappearingTextDelay) {
                config.disappearingTextDelay = it
                "Disappearing Text Delay: ${"%.2f".format(it)} seconds"
            }
                .constrain { defaultButtonConstraints() }

            val disappearingTextFade = slider(0.2f, 60f, config.disappearingTextFade) {
                config.disappearingTextFade = it
                "Disappearing Text Fade: ${"%.2f".format(it)} seconds"
            }
                .constrain { defaultButtonConstraints() }

            toggleButton("Disappearing Text", config.disappearingText) {
                config.disappearingText = it
                if (!it) {
                    disappearingTextDelay.hide()
                    disappearingTextFade.hide()
                } else {
                    disappearingTextDelay.unhide()
                    disappearingTextFade.unhide()
                }
            }
                .constrain { defaultButtonConstraints() } childOf this

            disappearingTextDelay childOf this
            disappearingTextFade childOf this

            if (!config.disappearingText) {
                disappearingTextDelay.hide()
                disappearingTextFade.hide()
            } else {
                disappearingTextDelay.unhide()
                disappearingTextFade.unhide()
            }
        } childOf this

        // Common Section
        expandableSection(I18n.get("gui.unitytranslate.config.common")) {
            val config = UnityTranslate.Companion.instance.config.common

            toggleButton("Should use CUDA?", config.shouldUseCuda) { config.shouldUseCuda = it }
                .constrain { defaultButtonConstraints() } childOf this

            slider(0.5f, 5f, config.batchTranslateInterval) {
                config.batchTranslateInterval = it
                "Batch Translate Interval: ${(it * 100).toInt()}ms"
            }.constrain { defaultButtonConstraints() } childOf this

            slider(1, 100, config.maxConcurrentTranslations) {
                config.maxConcurrentTranslations = it
                "Max Concurrent Translations: $it"
            }.constrain { defaultButtonConstraints() } childOf this

            slider(128, 16384, config.maxTextLength) {
                config.maxTextLength = it
                "Max Text Length: $it"
            }.constrain { defaultButtonConstraints() } childOf this
        } childOf this

        // Server Section
        expandableSection(I18n.get("gui.unitytranslate.config.server")) {

        } childOf this
    } childOf window

    init {
        sections.withScrollbar()

        copyFrom?.sections?.allChildren?.forEachIndexed { i, child ->
            if (child is ExpandableSection) {
                (sections.allChildren[i] as ExpandableSection).setExpanded(child.isExpanded)
            }
        }
    }

    val doneSection = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 100.percentOfWindow - 32.pixels
        this.width = 70.percentOfWindow
        this.height = 20.pixels
    }.apply {
        button("Save & Exit")
            .constrain {
                this.x = CenterConstraint() - 10.percent - 12.pixels
                this.width = 20.percent
            } childOf this

        button("Reset All to Default")
            .constrain {
                this.x = CenterConstraint() + 10.percent + 12.pixels
                this.width = 20.percent
            } childOf this
    } childOf window

    override fun onClose() {
        UnityTranslateMCClient.instance.saveConfig()
        UnityTranslateMCClient.instance.updateConfig()
        Minecraft.getInstance().setScreen(parent)
    }
}