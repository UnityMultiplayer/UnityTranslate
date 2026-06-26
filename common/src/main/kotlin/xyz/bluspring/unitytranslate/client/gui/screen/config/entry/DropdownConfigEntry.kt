package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.config.values.DropdownValidatingReflectingConfigValue

class DropdownConfigEntry<E>(value: DropdownValidatingReflectingConfigValue<E>) : ConfigEntry<E, DropdownValidatingReflectingConfigValue<E>>(value) {
    override fun submit(graphics: UIGraphics, partialTick: Float, areaWidth: Int, mouseX: Int, mouseY: Int): Int {
        return super.submit(graphics, partialTick, areaWidth, mouseX, mouseY)
    }
}
