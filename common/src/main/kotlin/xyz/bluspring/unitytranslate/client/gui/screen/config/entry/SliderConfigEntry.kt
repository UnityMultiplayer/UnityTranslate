package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.unitytranslate.api.v2.client.gui.element.SliderElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.config.builders.ConfigValueBuilderImpl
import xyz.bluspring.unitytranslate.config.values.ValidatingRangedConfigValue

class SliderConfigEntry<E : Number>(
    xPos: Float, yPos: Float,
    minWidth: Float, minHeight: Float,
    value: ValidatingRangedConfigValue<E>,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : ConfigEntry<E, ValidatingRangedConfigValue<E>>(xPos, yPos, minWidth, minHeight, value, rootKey, font) {
    override fun init(width: Int, height: Int) {
        super.init(width, height)

        this.addChild(SliderElement(this.xPos + this.label.bounds().width + 8, this.yPos - 7, this.minWidth, this.minHeight, this.value.property, this.value.min, this.value.max, this.value.step, this.font, (this.value.validator as ConfigValueBuilderImpl<E>).formatter ?: {
            TextComponent.literal(if (it is Int) "$it" else "%.2f".format(it))
        }))
    }
}
