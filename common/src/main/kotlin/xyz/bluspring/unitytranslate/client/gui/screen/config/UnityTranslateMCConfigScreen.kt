package xyz.bluspring.unitytranslate.client.gui.screen.config

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class UnityTranslateMCConfigScreen : Screen(Component.empty()) {
    val actualScreen = UnityTranslateConfigScreen()

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {

    }
}
