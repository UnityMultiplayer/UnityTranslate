package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.SliderElement
import xyz.bluspring.unitytranslate.config.builders.ConfigValueBuilderImpl
import xyz.bluspring.unitytranslate.config.values.ValidatingRangedConfigValue

class SliderConfigEntry<E : Number>(
    xPos: Float, yPos: Float,
    width: Float, height: Float,
    value: ValidatingRangedConfigValue<E>,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : ConfigEntry<E, ValidatingRangedConfigValue<E>>(xPos, yPos, width, height, value, rootKey, font) {
    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.addChild(SliderElement(this.xPos, this.yPos + 6, this.width, this.height, this.value.property, this.value.min, this.value.max, this.value.step, this.font, (this.value.validator as ConfigValueBuilderImpl<E>).formatter ?: {
            Component.literal(if (it is Int) "$it" else "%.2f".format(it))
        }))
    }
}
