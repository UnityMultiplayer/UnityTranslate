package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap
import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy

open class ConfigCategoryEntry(
    xPos: Float, yPos: Float,
    minWidth: Float, minHeight: Float,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
    category: ConfigCategory,
) : ConfigEntry<List<ConfigValue<*>>, ConfigCategory>(
    xPos, yPos, minWidth, minHeight, category, rootKey, font
) {
    private var hoveredEntry: ConfigEntry<*, *>? = null
    private val storedHeights = Reference2IntLinkedOpenHashMap<ConfigEntry<*, *>>()
    private var scrollAmount = 0.0

    override fun init(width: Int, height: Int) {
        super.init(width, height)

        var yOffset = 0f
        val entries = this.value.value.map {
            val entry = fromValue(it, xPos, yPos + yOffset, minWidth, minHeight, rootKey, font)
            yOffset += entry.bounds().height

            entry
        }

        var yPos = this.yPos
        for (entry in entries) {
            this.addChild(entry)
            yPos += entry.bounds().height + 4
        }
    }
}
