package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.sunset.values.RangedConfigValue
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy

class SliderConfigEntry<E : Number>(
    xPos: Float, yPos: Float,
    width: Float, height: Float,
    value: RangedConfigValue<E>,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : ConfigEntry<E, RangedConfigValue<E>>(xPos, yPos, width, height, value, rootKey, font) {

}
