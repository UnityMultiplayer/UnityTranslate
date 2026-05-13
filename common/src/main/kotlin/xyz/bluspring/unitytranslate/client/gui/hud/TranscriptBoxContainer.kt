package xyz.bluspring.unitytranslate.client.gui.hud

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.RainbowColorConstraint
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.effects.RoundedOutlineEffect
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.config.ColorConfig
import xyz.bluspring.unitytranslate.config.TranscriptBoxConfig
import java.awt.Color

class TranscriptBoxContainer(
    val holder: TranscriptHolder,
    val config: TranscriptBoxConfig
) : UIRoundedRectangle(config.cornerRadius) {
    private val roundedEffect = RoundedOutlineEffect(config.outline.thickness, config.cornerRadius, Color((config.outline.color as ColorConfig.Solid).color, true))

    init {
        effect(roundedEffect)
        this.update()
    }

    fun update() {
        val screenWidth = ClientPlatformProxy.instance.windowWidth
        val screenHeight = ClientPlatformProxy.instance.windowHeight

        val pos = config.transforms.position.calculatePos(screenWidth, screenHeight)
        val dimensions = config.transforms.size.calculateDimensions(pos, screenWidth, screenHeight)

        this.constraints.x = (pos.x - dimensions.left).pixels
        this.constraints.y = (pos.y - dimensions.top).pixels
        this.constraints.width = (dimensions.right - dimensions.left).pixels
        this.constraints.height = (dimensions.bottom - dimensions.top).pixels
        this.setRadius(config.cornerRadius.pixels)
        this.constraints.color = RainbowColorConstraint()
        this.roundedEffect.thickness = config.outline.thickness
        this.roundedEffect.color = Color((config.outline.color as ColorConfig.Solid).color, true)
        this.roundedEffect.radius = config.cornerRadius
    }
}
