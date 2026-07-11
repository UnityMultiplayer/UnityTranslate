package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.ToggleButton
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig
import xyz.bluspring.unitytranslate.config.values.ValidatingReflectingConfigValue

class ToggleConfigEntry(
    xPos: Float, yPos: Float,
    width: Float, height: Float,
    value: ValidatingReflectingConfigValue<Boolean>,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : ConfigEntry<Boolean, ValidatingReflectingConfigValue<Boolean>>(xPos, yPos, width, height, value, rootKey, font) {
    private lateinit var button: ToggleButton

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.button = this.addChild(ToggleButton(this.xPos, this.yPos + 7, 12f, this.value.property, ThemeConfig.toggleOutline,
            ThemeConfig.toggleOutlineFocused, ThemeConfig.toggleDisabledFill, ThemeConfig.toggleEnabledFill))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            return this.button.mouseClicked(this.button.x + (this.button.size / 2.0), this.button.y + (this.button.size / 2.0), button)
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
