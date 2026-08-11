package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.util.NonConfigValue

class UnknownConfigValueEntry(
    xPos: Float, yPos: Float,
    minWidth: Float, minHeight: Float,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
    id: String,
) : ConfigEntry<Unit, NonConfigValue>(xPos, yPos, minWidth, minHeight, NonConfigValue(id), rootKey, font) {
    override fun init(width: Int, height: Int) {
        super.init(width, height)
    }
}
