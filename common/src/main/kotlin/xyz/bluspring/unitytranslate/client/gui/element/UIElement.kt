package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics

abstract class UIElement {
    protected val children = mutableListOf<UIElement>()

    protected var isSelected = false

    abstract val bounds: ScreenRectangle
    abstract fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int)

    open fun tick() {}

    open fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        return false
    }

    open fun keyReleased(key: Int, scanCode: Int, modifiers: Int): Boolean {
        return false
    }

    open fun charTyped(codepoint: Int): Boolean {
        return false
    }

    open fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return false
    }

    open fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return false
    }

    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return false
    }
}
