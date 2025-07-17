package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.UIConstraints
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.RoundedOutlineEffect
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.button
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.constraints.CramAwareChildBasedSizeConstraint
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.expandableSection
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.toggleButton
import java.awt.Color

class UTConfigScreen(private val parent: Screen?) : WindowScreen(ElementaVersion.V10) {
    val topText = UIText("UnityTranslate").constrain {
        this.x = CenterConstraint()
        this.y = 12.pixels
    } childOf window

    val sections = ScrollComponent().constrain {
        this.x = CenterConstraint()
        this.y = 24.pixels
        this.width = 90.percentOfWindow
        this.height = 90.percentOfWindow
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

            toggleButton("Mute Transcript when Voice Chat Muted?", config.muteTranscriptWhenVoiceChatMuted) { config.muteTranscriptWhenVoiceChatMuted = it }
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
        } childOf this

        // Common Section
        expandableSection(I18n.get("gui.unitytranslate.config.common")) {
            val config = UnityTranslate.Companion.instance.config.common

            toggleButton("Should use CUDA?", config.shouldUseCuda) { config.shouldUseCuda = it }
                .constrain { defaultButtonConstraints() } childOf this
        } childOf this

        // Server Section
        expandableSection(I18n.get("gui.unitytranslate.config.server")) {

        } childOf this
    } childOf window

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
        Minecraft.getInstance().setScreen(parent)
    }
}