package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy

abstract class UIElement {
    protected val children = mutableListOf<UIElement>()

    protected var isSelected = false

    fun bounds(): ScreenRectangle = bounds(ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
    abstract fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle

    open fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        for (element in this.children) {
            element.submit(graphics, partialTick, mouseX, mouseY)
        }
    }

    open fun tick() {
        for (element in this.children) {
            element.tick()
        }
    }

    open fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        for (element in this.children) {
            if (element.keyPressed(key, scanCode, modifiers))
                return true
        }

        return false
    }

    open fun keyReleased(key: Int, scanCode: Int, modifiers: Int): Boolean {
        for (element in this.children) {
            if (element.keyReleased(key, scanCode, modifiers))
                return true
        }

        return false
    }

    open fun charTyped(codepoint: Int): Boolean {
        for (element in this.children) {
            if (element.charTyped(codepoint))
                return true
        }

        return false
    }

    open fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in this.children) {
            if (element.mouseClicked(mouseX, mouseY, button))
                return true
        }

        return false
    }

    open fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in this.children) {
            if (element.mouseReleased(mouseX, mouseY, button))
                return true
        }

        return false
    }

    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        for (element in this.children) {
            if (element.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
                return true
        }

        return false
    }
}
