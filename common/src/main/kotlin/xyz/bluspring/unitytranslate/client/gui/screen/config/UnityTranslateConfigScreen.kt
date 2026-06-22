package xyz.bluspring.unitytranslate.client.gui.screen.config

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

class UnityTranslateConfigScreen : UTScreen() {
    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val font = Minecraft.getInstance().font

        val keys = UnityTranslateApiImpl.configs.keys.toMutableList()
        keys.remove("unitytranslate")

        var offsetY = 16f

        graphics.drawCenteredString(font, Component.translatable("config.unitytranslate.unitytranslate").withStyle { it.withBold(true) }, graphics.width / 8f, offsetY, -1, true)
        offsetY += 16

        offsetY = this.drawSection(graphics, "unitytranslate", UnityTranslateApiImpl.configs["unitytranslate"]!!, font, offsetY)

        if (keys.isNotEmpty()) {
            offsetY += 12
            graphics.drawCenteredString(font, Component.translatable("config.unitytranslate.plugins").withStyle { it.withBold(true) }, graphics.width / 8f, offsetY, -1, true)
            offsetY += 16

            for (key in keys.sorted()) {
                offsetY = this.drawSection(graphics, key, UnityTranslateApiImpl.configs[key]!!, font, offsetY)
            }
        }
    }

    private fun drawSection(graphics: UIGraphics, sectionId: String, configSection: SunsetConfig, font: Font, offsetYFinal: Float): Float {
        var offsetY = offsetYFinal

        for (configValue in configSection.rootCategory.value) {
            graphics.drawString(font, Component.translatable("config.unitytranslate.$sectionId${configValue.fullId}"), 8f, offsetY, -1, true)
            offsetY += font.lineHeight + 4

            if (configValue is ConfigCategory) {
                for (configValue2 in configValue.value) {
                    graphics.drawString(font, Component.translatable("config.unitytranslate.$sectionId${configValue2.fullId}"), 16f, offsetY, -1, true)
                    offsetY += font.lineHeight + 4
                }
            }
        }

        return offsetY
    }
}
