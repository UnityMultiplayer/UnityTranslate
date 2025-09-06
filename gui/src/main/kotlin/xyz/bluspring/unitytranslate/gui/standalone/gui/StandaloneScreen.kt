package xyz.bluspring.unitytranslate.gui.standalone.gui

import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UScreen
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui
import xyz.bluspring.unitytranslate.gui.menu.ContextMenuRenderer
import xyz.bluspring.unitytranslate.gui.menu.LayeredScreenListener
import xyz.bluspring.unitytranslate.gui.menu.tutorial.TutorialRenderer
import xyz.bluspring.unitytranslate.gui.transcriber.browser.BrowserNativeHandles

class StandaloneScreen : UScreen(), LayeredScreenListener {
    val tutorialRenderer = TutorialRenderer()
    val contextMenuRenderer = ContextMenuRenderer()

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        UnityTranslateGui.renderClear()
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)

        UnityTranslateGui.transcriptRenderer.render(matrixStack, partialTicks)

        // Render context box over everything
        contextMenuRenderer.render(matrixStack, mouseX, mouseY, partialTicks)

        // Except for the tutorial renderer, which we should handle above all else
        tutorialRenderer.window.draw(matrixStack)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        // Handle tutorial clicks, as that should take priority.
        if (tutorialRenderer.mouseClick(mouseX, mouseY, mouseButton))
            return

        // Handle context box stuff before everything
        if (contextMenuRenderer.onMouseClicked(mouseX, mouseY, mouseButton))
            return

        super.onMouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        // Refresh screen
        if (keyCode == UKeyboard.KEY_F5) {
            UnityTranslateGui.reloadRenderers()
            return
        }

        // kill driver windows, cuz dear god
        if (keyCode == UKeyboard.KEY_F8) {
            BrowserNativeHandles.killOlderDriverWindows()
            return
        }

        super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    override fun initScreen(width: Int, height: Int) {
        tutorialRenderer.window.onWindowResize()
        contextMenuRenderer.window.onWindowResize()
        super.initScreen(width, height)
    }

    override fun onScreenLayerOpened() {
        super.onScreenLayerOpened()
        UnityTranslateGui.reloadRenderers()
    }

    override fun onScreenLayerClosed() {
        super.onScreenLayerClosed()
        contextMenuRenderer.closeContextMenu()
    }
}