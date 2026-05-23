package xyz.bluspring.unitytranslate.client.gui.hud

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.effects.RoundedOutlineEffect
import gg.essential.universal.UMatrixStack
import xyz.bluspring.fork.elementa.ElementaClientPlatformProxy
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import java.awt.Color

class TranscriptBoxContainer(
    val holder: TranscriptHolder,
    val config: TranscriptBoxConfig
) : UIRoundedRectangle(config.cornerRadius) {
    // TODO: these should not be dependent on solid colours.
    private val roundedEffect = RoundedOutlineEffect(
        config.outline.thickness,
        config.cornerRadius,
        Color((config.outline.color as ColorConfig.Solid).color, true)
    )

    init {
        effect(roundedEffect)
        this.updateConfig()
    }

    fun updateConfig() {
        val screenWidth = ElementaClientPlatformProxy.instance.windowWidth
        val screenHeight = ElementaClientPlatformProxy.instance.windowHeight

        val pos = config.transforms.position.calculatePos(screenWidth, screenHeight)
        val dimensions = config.transforms.size.calculateDimensions(pos, screenWidth, screenHeight)

        this.constraints.x = (pos.x - dimensions.left).pixels
        this.constraints.y = (pos.y - dimensions.top).pixels
        this.constraints.width = (dimensions.right - dimensions.left).pixels
        this.constraints.height = (dimensions.bottom - dimensions.top).pixels
        this.setRadius(config.cornerRadius.pixels)
        this.constraints.color = ConstantColorConstraint(
            Color(
                ((config.background as TranscriptBoxConfig.Background.Color).color as ColorConfig.Solid).color,
                true
            )
        )
        this.roundedEffect.thickness = config.outline.thickness
        this.roundedEffect.color = Color((config.outline.color as ColorConfig.Solid).color, true)
        this.roundedEffect.radius = config.cornerRadius
        this.clearChildren()

        UIText(this.config.header.display.text(this.holder.languageCode).copy().withStyle(this.config.header.style)).constrain {

        } childOf this
    }

    override fun beforeDraw(matrixStack: UMatrixStack) {
        for (data in this.holder.transcripts) {

        }

        super.beforeDraw(matrixStack)
    }
}
