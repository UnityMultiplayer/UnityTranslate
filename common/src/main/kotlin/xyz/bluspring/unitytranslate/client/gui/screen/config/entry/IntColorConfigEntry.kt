package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.config.values.IntColorConfigValue

class IntColorConfigEntry(
    xPos: Float, yPos: Float,
    minWidth: Float, minHeight: Float,
    value: IntColorConfigValue,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : ConfigEntry<Int, IntColorConfigValue>(xPos, yPos, minWidth, minHeight, value, rootKey, font) {
    override fun init(width: Int, height: Int) {
        super.init(width, height)
    }
}
