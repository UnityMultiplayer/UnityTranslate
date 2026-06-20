package xyz.bluspring.unitytranslate.client.gui.screen.config

import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.unitytranslate.client.gui.screen.config.entry.ConfigCategoryEntry
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

class ConfigSection(category: ConfigCategory) : ConfigCategoryEntry(category) {
    override fun submitAndGetHeightOffset(graphics: UIGraphics, partialTick: Float, areaWidth: Int, mouseX: Int, mouseY: Int): Float {
        return 0f
    }
}
