package xyz.bluspring.unitytranslate.client.gui.screen.config

import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import kotlinx.coroutines.Deferred
import net.minecraft.client.Minecraft
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen

class UnityTranslateConfigScreen : UTScreen() {
    private val sections = mutableListOf<ConfigSection>()

    val focused = ReferenceArraySet<ConfigSection>()
    val queuedTasks = mutableMapOf<String, Deferred<*>>()

    init {
        this.sections += ConfigSection(this, "unitytranslate", listOf(UnityTranslateApiImpl.configs["unitytranslate"]!!))

        val keys = UnityTranslateApiImpl.configs.keys.toMutableList()
        keys.remove("unitytranslate")
        keys.remove("unitytranslate_theme")
        val pluginConfigs = mutableListOf<SunsetConfig>()

        for (key in keys.sorted()) {
            pluginConfigs += UnityTranslateApiImpl.configs[key]!!
        }

        this.sections += ConfigSection(this, "plugins", pluginConfigs)
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        graphics.fill(0f, 0f, graphics.width.toFloat(), graphics.height.toFloat(),
            ARGBHelper.colorFromFloat(0.2f, 0f, 0f, 0f),
            ARGBHelper.colorFromFloat(0.6f, 0f, 0f, 0f)
        )

        val font = Minecraft.getInstance().font

        val sectionHeight = this.sections.sumOf { it.calculateSidebarHeight(font).toDouble() + 8.0 }.toFloat() + 16f // Kotlin why do you not permit floats in this?

        graphics.pushMatrix()
        if (sectionHeight < graphics.height)
            graphics.translate(0f, graphics.height / 2f - (sectionHeight / 2f))

        var offsetY = 0f
        for (section in this.sections) {
            offsetY += 8f
            offsetY = section.submitSidebar(graphics, font, partialTick, offsetY)
        }

        graphics.popMatrix()
    }
}
