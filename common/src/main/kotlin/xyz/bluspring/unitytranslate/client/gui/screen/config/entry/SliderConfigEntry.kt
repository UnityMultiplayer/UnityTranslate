package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.sunset.values.RangedConfigValue
import xyz.bluspring.unitytranslate.client.renderer.ui.UIGraphics

class SliderConfigEntry<E : Number>(value: RangedConfigValue<E>) : ConfigEntry<E, RangedConfigValue<E>>(value) {
    override fun submit(graphics: UIGraphics, partialTick: Float, areaWidth: Int, mouseX: Int, mouseY: Int): Int {
        return super.submit(graphics, partialTick, areaWidth, mouseX, mouseY)
    }
}
