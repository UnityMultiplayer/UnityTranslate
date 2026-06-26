package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen
import xyz.bluspring.unitytranslate.client.renderer.ui.MinecraftUIGraphics

class WrappedUTScreen(val actualScreen: UTScreen, private val parent: Screen? = null) : Screen(Component.empty()) {
    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (this.minecraft.level == null) {
            this.extractPanorama(graphics, a)
        }

        this.extractBlurredBackground(graphics)
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        actualScreen.submit(MinecraftUIGraphics(graphics), a, mouseX, mouseY)
    }
}
