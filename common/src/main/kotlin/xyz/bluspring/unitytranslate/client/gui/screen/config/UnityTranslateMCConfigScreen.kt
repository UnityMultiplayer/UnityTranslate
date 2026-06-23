package xyz.bluspring.unitytranslate.client.gui.screen.config

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.client.renderer.BatchedGuiRenderer
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

class UnityTranslateMCConfigScreen(private val parent: Screen) : Screen(Component.empty()) {
    val actualScreen = UnityTranslateConfigScreen()

    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (this.minecraft.level == null) {
            this.extractPanorama(graphics, a)
        }

        this.extractBlurredBackground(graphics)
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0)
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        actualScreen.submit(UIGraphics(BatchedGuiRenderer.DrawLayer.SCREEN), a, mouseX, mouseY)
    }
}
