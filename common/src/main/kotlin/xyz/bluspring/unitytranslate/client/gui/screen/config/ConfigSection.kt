package xyz.bluspring.unitytranslate.client.gui.screen.config

import net.minecraft.client.gui.Font
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.screen.config.entry.ConfigEntry
import xyz.bluspring.unitytranslate.config.values.HiddenConfigValue

class ConfigSection(val parent: UnityTranslateConfigScreen, val id: String, val config: Collection<SunsetConfig>) : UIElement() {
    override fun init(width: Int, height: Int) {
        super.init(width, height)
        val font = ClientPlatformProxy.instance.defaultFont
        var offsetY = font.lineHeight + 2f

        for (config in this.config) {
            for (value in config.rootCategory.value) {
                val entry = ConfigEntry.fromValue(value, 175f + 12, offsetY, 100f, font.lineHeight.toFloat(), "config.unitytranslate.$id", font)
                offsetY += entry.bounds().height + 4

                this.addChild(entry)
            }
        }
    }

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return ScreenRectangle(175 + 12, 0, this.children.sumOf { it.getBounds(screenWidth, screenHeight).width }, this.children.sumOf { it.getBounds(screenWidth, screenHeight).height })
    }

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
        if (value is HiddenConfigValue) return 0f

        var offsetY = 0f
        offsetY += font.lineHeight + 4

        if (value is ConfigCategory) {
            for (configValue in value.value) {
                offsetY += this.recursiveCalculateSidebarHeight(font, configValue)
            }
        }

        return offsetY
    }

    fun submitSidebar(graphics: UIGraphics, font: FontReference, partialTick: Float, offsetYFinal: Float): Float {
        var offsetY = offsetYFinal
        graphics.centeredText(font, TextComponent.translatable("config.unitytranslate.$id")
            .withStyle { it.withBold(true) }, 87.5f, offsetY, -1, true)
        offsetY += 16

        for (config in this.config) {
            for (configValue in config.rootCategory.value) {
                if (configValue is HiddenConfigValue) continue

                offsetY += 2
                offsetY = this.recursiveSubmitSidebarEntry(graphics, font, configValue, 8, offsetY)
            }
        }

        return offsetY
    }

    private fun recursiveSubmitSidebarEntry(graphics: UIGraphics, font: FontReference, value: ConfigValue<*>, offsetX: Int, offsetYFinal: Float): Float {
        if (value is HiddenConfigValue) return 0f

        var offsetY = offsetYFinal
        val distance = 1f - (((offsetX - 8f) / 16f) / 6f)

        graphics.text(font, font.substr(TextComponent.translatable("config.unitytranslate.$id${value.fullId}"), 175 - offsetX), offsetX.toFloat(), offsetY,
            ARGBHelper.colorFromFloat(1f, distance, distance, distance), true)
        offsetY += font.lineHeight + 4

        if (value is ConfigCategory) {
            for (configValue in value.value) {
                if (configValue is HiddenConfigValue) continue
                offsetY = recursiveSubmitSidebarEntry(graphics, font, configValue, offsetX + 16, offsetY)
            }
        }

        return offsetY
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        graphics.centeredText(ClientPlatformProxy.instance.defaultFont, TextComponent.translatable("config.unitytranslate.$id")
            .withStyle { it.withBold(true) }, graphics.width / 2f, 0f, -1, true)

        graphics.pushMatrix()
        graphics.translate(0f, 8f)
        super.submit(graphics, partialTick, mouseX, mouseY)
        graphics.popMatrix()
    }
}
