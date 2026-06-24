package xyz.bluspring.unitytranslate.client.gui.screen.config

import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.minecraft.client.Minecraft
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen
import xyz.bluspring.unitytranslate.client.renderer.ui.UIGraphics

class UnityTranslateConfigScreen : UTScreen() {
    private val sections = mutableListOf<ConfigSection>()

    val focused = ReferenceArraySet<ConfigSection>()

    init {
        this.sections += ConfigSection("unitytranslate", listOf(UnityTranslateApiImpl.configs["unitytranslate"]!!))

        val keys = UnityTranslateApiImpl.configs.keys.toMutableList()
        keys.remove("unitytranslate")
        val pluginConfigs = mutableListOf<SunsetConfig>()

        for (key in keys.sorted()) {
            pluginConfigs += UnityTranslateApiImpl.configs[key]!!
        }

        this.sections += ConfigSection("plugins", pluginConfigs)
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        graphics.fill(0f, 0f, graphics.width.toFloat(), graphics.height.toFloat(),
            ARGBHelper.colorFromFloat(0.2f, 0f, 0f, 0f),
            ARGBHelper.colorFromFloat(0.6f, 0f, 0f, 0f)
        )

        val font = Minecraft.getInstance().font

        val sectionHeight = this.sections.sumOf { it.calculateSidebarHeight(font).toDouble() + 8.0 }.toFloat() + 16f // Kotlin why do you not permit floats in this?

        graphics.poseStack.pushPose()
        graphics.poseStack.translate(0f, graphics.height / 2f - (sectionHeight / 2f), 0f)

        var offsetY = 0f
        for (section in this.sections) {
            offsetY += 8f
            offsetY = section.submitSidebar(graphics, font, partialTick, offsetY)
        }

        graphics.poseStack.popPose()
    }
}
