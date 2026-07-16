package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.gui.element.FocusableUIElement
import xyz.bluspring.unitytranslate.client.gui.element.UIElement
import xyz.bluspring.unitytranslate.util.ScreenUtil

class ContextBox(val x: Float, val y: Float, val maxWidth: Int = 150, val elements: Collection<ContextBoxElement>, val shouldScale: Boolean = true) : UIElement(), FocusableUIElement {
    override var isFocused: Boolean = false
        get() = field || (this.children.any { it is FocusableUIElement && it.isFocused })

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
        if (this.shouldScale)
            graphics.scale(0.5f, 0.5f)
        super.submit(graphics, partialTick, ((mouseX - this.x) * if (this.shouldScale) 2f else 1f).toInt(), ((mouseY - this.y) * if (this.shouldScale) 2f else 1f).toInt())
        graphics.popMatrix()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return super.mouseClicked(((mouseX - this.x) * if (this.shouldScale) 2.0 else 1.0), ((mouseY - this.y) * if (this.shouldScale) 2.0 else 1.0), button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return super.mouseReleased(((mouseX - this.x) * if (this.shouldScale) 2.0 else 1.0), ((mouseY - this.y) * if (this.shouldScale) 2.0 else 1.0), button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        return super.mouseScrolled(((mouseX - this.x) * if (this.shouldScale) 2.0 else 1.0), ((mouseY - this.y) * if (this.shouldScale) 2.0 else 1.0), scrollX, scrollY)
    }

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return ScreenUtil.union(*this.children.map { it.getBounds(screenWidth, screenHeight) }.toTypedArray())
    }
}
