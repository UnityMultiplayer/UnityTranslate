package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference

class DropdownList<E>(
    val x: Float, val y: Float,
    val width: Float, val height: Float,

    val font: FontReference,
    elements: Collection<E>,
    val visualizer: (E) -> Component,
) : UIElement() {
    val elements = elements.toList()
    private var currentIndex = 0
    private var isOpened = false

    val selected: E
        get() = this.elements[this.currentIndex]

    private val mainBounds = ScreenRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return this.mainBounds
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        graphics.fill(x, y + height, x + width, y + height + 2, -1) // underline
        graphics.text(this.font, this.font.split(this.visualizer(this.selected), this.width.toInt() - 30)[0], this.x, this.y, -1, true)
        graphics.text(this.font, Component.literal(if (this.isOpened) "▲" else "▼"), this.x + this.width - 10, this.y + (this.height / 2), -1, true)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.mainBounds.containsPoint(mouseX.toInt(), mouseY.toInt())) {
            this.isOpened = !this.isOpened
            return true
        } else if (this.isOpened) {
            this.isOpened = false
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        if (this.isOpened) {
            if (key == GLFW.GLFW_KEY_UP) {
                this.currentIndex--

                if (this.currentIndex < 0)
                    this.currentIndex = this.elements.lastIndex

                return true
            } else if (key == GLFW.GLFW_KEY_DOWN) {
                this.currentIndex++

                if (this.currentIndex > this.elements.lastIndex)
                    this.currentIndex = 0

                return true
            }
        }

        return super.keyPressed(key, scanCode, modifiers)
    }
}
