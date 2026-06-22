package xyz.bluspring.unitytranslate.client.gui.screen.config

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

class UnityTranslateMCConfigScreen(private val parent: Screen) : Screen(Component.empty()) {
    val actualScreen = UnityTranslateConfigScreen()

    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        actualScreen.submit(UIGraphics(), a, mouseX, mouseY)
    }
}
