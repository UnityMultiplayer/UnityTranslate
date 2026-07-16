package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.gui.element.FocusableUIElement
import xyz.bluspring.unitytranslate.client.gui.element.UIElement
import xyz.bluspring.unitytranslate.util.ScreenUtil

class ContextBox(val x: Float, val y: Float, val maxWidth: Int = 150, val elements: Collection<ContextBoxElement>) : UIElement(), FocusableUIElement {
    override var isFocused: Boolean = false

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        for (element in this.elements) {
            element.x = 0f
            element.y = (this.children.maxOfOrNull { it.getBounds(width, height).bottom() } ?: 0f).toFloat()
            element.width = this.maxWidth.toFloat()
            this.addChild(element)
        }
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        graphics.pushMatrix()
        graphics.translate(this.x, this.y)
        graphics.scale(0.5f, 0.5f)
        super.submit(graphics, partialTick, ((mouseX - this.x) * 2f).toInt(), ((mouseY - this.y) * 2f).toInt())
        graphics.popMatrix()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return super.mouseClicked(((mouseX - this.x) * 2.0), ((mouseY - this.y) * 2.0), button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return super.mouseReleased(((mouseX - this.x) * 2.0), ((mouseY - this.y) * 2.0), button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return super.mouseScrolled(((mouseX - this.x) * 2.0), ((mouseY - this.y) * 2.0), scrollX, scrollY)
    }

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return ScreenUtil.union(*this.children.map { it.getBounds(screenWidth, screenHeight) }.toTypedArray())
    }
}
