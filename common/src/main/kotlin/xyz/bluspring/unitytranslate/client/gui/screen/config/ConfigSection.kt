package xyz.bluspring.unitytranslate.client.gui.screen.config

import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

class ConfigSection(val id: String, val config: Collection<SunsetConfig>) {
    fun calculateSidebarHeight(font: Font): Float {
        var offsetY = 16f

        for (config in this.config) {
            for (value in config.rootCategory.value) {
                offsetY += 2
                offsetY += this.recursiveCalculateSidebarHeight(font, value)
            }
        }

        return offsetY
    }

    private fun recursiveCalculateSidebarHeight(font: Font, value: ConfigValue<*>): Float {
        var offsetY = 0f
        offsetY += font.lineHeight + 4

        if (value is ConfigCategory) {
            for (configValue in value.value) {
                offsetY += this.recursiveCalculateSidebarHeight(font, configValue)
            }
        }

        return offsetY
    }

    fun submitSidebar(graphics: UIGraphics, font: Font, partialTick: Float, offsetYFinal: Float): Float {
        var offsetY = offsetYFinal
        graphics.drawCenteredString(font, Component.translatable("config.unitytranslate.$id")
            .withStyle { it.withBold(true) }, 87.5f, offsetY, -1, true)
        offsetY += 16

        for (config in this.config) {
            for (configValue in config.rootCategory.value) {
                offsetY += 2

                offsetY = this.recursiveSubmitSidebarEntry(graphics, font, configValue, 8, offsetY)
            }
        }

        return offsetY
    }

    private fun recursiveSubmitSidebarEntry(graphics: UIGraphics, font: Font, value: ConfigValue<*>, offsetX: Int, offsetYFinal: Float): Float {
        var offsetY = offsetYFinal

        graphics.drawString(font, font.substrByWidth(Component.translatable("config.unitytranslate.$id${value.fullId}"), 175 - offsetX), offsetX.toFloat(), offsetY, -1, true)
        offsetY += font.lineHeight + 4

        if (value is ConfigCategory) {
            for (configValue in value.value) {
                offsetY = recursiveSubmitSidebarEntry(graphics, font, configValue, offsetX + 16, offsetY)
            }
        }

        return offsetY
    }
}
