package xyz.bluspring.unitytranslate.minecraft.client.gui

import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.utils.toFormattedString
import net.minecraft.ChatFormatting
import net.minecraft.client.resources.language.I18n
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.minecraft.client.TranscriptHolder
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.colorWithAlpha
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.constraints.AlignTypeBasedXConstraint
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.constraints.AlignTypeBasedYConstraint
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects.RoundedOutlineEffect
import java.awt.Color

class TranscriptBox(val holder: TranscriptHolder, val config: UnityTranslateClientConfig.TranscriptBoxConfig) : UIContainer() {
    val background = UIRoundedRectangle(0f)
        .constrain {
            this.x = 0.pixels
            this.y = 0.pixels
            this.width = 100.percent
            this.height = 100.percent
            this.color = colorWithAlpha(config.color, config.opacity).constraint
        } childOf this

    val innerContainer = UIContainer()
        .constrain {
            this.x = 0.pixels
            this.y = 0.pixels
            this.width = 100.percent
            this.height = 100.percent
        } childOf this

    val topText = UIWrappedText("placeholder", centered = true)
        .constrain {
            this.x = CenterConstraint()
            this.y = 4.pixels
        } childOf innerContainer

    val transcriptContainer = ScrollComponent(innerPadding = 1f, verticalScrollOpposite = true, verticalScrollEnabled = false)
        .constrain {
            this.x = 0.pixels
            this.y = SiblingConstraint(4f)
            this.width = 100.percent
            this.height = FillConstraint()
        } childOf innerContainer

    private val renderedTranscripts = mutableSetOf<Transcript>()

    init {
        update()
    }

    fun update() {
        this.background.setColor(colorWithAlpha(config.color, (config.opacity - (if (config.outlineThickness > 0f) config.outlineOpacity else 0)).coerceAtLeast(0)))

        this.background.effects.clear()
        if (config.outlineThickness > 0f) {
            this.background.effect(RoundedOutlineEffect(config.outlineThickness, colorWithAlpha(config.outlineColor, config.outlineOpacity)))
        }

        this.constraints.width = config.width.pixels
        this.constraints.height = config.height.pixels

        this.constraints.x = AlignTypeBasedXConstraint(config.horizontalAlignType, config.offsetX)
        this.constraints.y = AlignTypeBasedYConstraint(config.verticalAlignType, config.offsetY)

        when (config.headerType) {
            UnityTranslateClientConfig.HeaderType.NONE -> {
                this.topText.hide()
            }
            UnityTranslateClientConfig.HeaderType.SHORT_LANG -> {
                this.topText.unhide()
                this.topText.setText(I18n.get("unitytranslate.transcript") + " (${config.language.code.uppercase()})")
            }
            UnityTranslateClientConfig.HeaderType.LONG_LANG -> {
                this.topText.unhide()
                this.topText.setText(I18n.get("unitytranslate.transcript") + " (${I18n.get(config.language.translationKey)})")
            }
        }

        for (child in transcriptContainer.children) {
            if (child is UIWrappedText) {
                child.constraints.textScale = config.textScale.percent
            }
        }
    }

    fun tick() {
        val clientConfig = UnityTranslateMCClient.clientConfig
        renderedTranscripts.removeIf { !holder.transcripts.contains(it) }

        for (transcript in holder.transcripts.reversed()) {
            val text = I18n.get("chat.type.text",
                (transcript.playerRef as Player).displayName.toFormattedString() + " ${ChatFormatting.GREEN}(${transcript.language.code.uppercase()})${ChatFormatting.RESET}",
                if (transcript.incomplete) "${ChatFormatting.GRAY}${ChatFormatting.ITALIC}" else "" + transcript.text
            )

            val element = UIWrappedText(text)
                .constrain {
                    this.x = 0.pixels
                    this.y = SiblingConstraint(3f)
                    this.width = 100.percent
                    this.textScale = config.textScale.percent
                } childOf transcriptContainer

            if (clientConfig.disappearingText) {
                element.animate {
                    setColorAnimation(Animations.IN_OUT_EXP, clientConfig.disappearingTextFade, Color(255, 255, 255, 0).constraint, clientConfig.disappearingTextDelay)
                }
            }
        }
    }
}