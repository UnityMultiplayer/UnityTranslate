package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap
import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics

open class ConfigCategoryEntry(category: ConfigCategory) : ConfigEntry<List<ConfigValue<*>>, ConfigCategory>(category) {
    val entries = category.value.map(Companion::fromValue)
    private var hoveredEntry: ConfigEntry<*, *>? = null
    private val storedHeights = Reference2IntLinkedOpenHashMap<ConfigEntry<*, *>>()
    private var scrollAmount = 0.0

    protected open fun submitAndGetHeightOffset(graphics: UIGraphics, partialTick: Float, areaWidth: Int, mouseX: Int, mouseY: Int): Float {
        return super.submit(graphics, partialTick, areaWidth, mouseX, mouseY).toFloat()
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, areaWidth: Int, mouseX: Int, mouseY: Int): Int {
        var offsetY = this.submitAndGetHeightOffset(graphics, partialTick, areaWidth, mouseX, mouseY)

        for (entry in entries) {
            graphics.pushMatrix()
            graphics.translate(0f, offsetY)
            val entryHeight = entry.submit(graphics, partialTick, areaWidth, mouseX, mouseY)
            this.storedHeights[entry] = entryHeight

            if (mouseX >= 0 && mouseY >= offsetY && mouseY <= offsetY + entryHeight) {
                hoveredEntry = entry
            }

            offsetY += entryHeight
            graphics.popMatrix()
        }

        return offsetY.toInt()
    }

    override fun mouseClick(mouseX: Int, mouseY: Int): Boolean {
        val hoveredEntry = this.hoveredEntry
        if (hoveredEntry != null) {
            return true
        }

        return super.mouseClick(mouseX, mouseY)
    }

    override fun mouseScroll(mouseX: Int, mouseY: Int, scrollY: Double): Boolean {
        if (hoveredEntry != null && hoveredEntry!!.mouseScroll(mouseX, mouseY, scrollY)) {
            return true
        }

        return super.mouseScroll(mouseX, mouseY, scrollY)
    }
}
