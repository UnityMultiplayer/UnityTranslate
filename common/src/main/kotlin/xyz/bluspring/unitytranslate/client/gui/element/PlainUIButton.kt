package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.multiplyAlpha
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

open class PlainUIButton(
    private val font: FontReference,
    private val text: Component,

    private val x: Float,
    private val y: Float,
    private val maxWidth: Int = 10000,
    open var color: Int = ThemeConfig.plainButton,
    protected val hoverColor: Int = ThemeConfig.plainButtonHover,
    private val onClick: () -> Unit,
) : UIElement(), FadeableUIElement {
    override var opacity: Float = 1f

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        val split = this.font.split(this.text, this.maxWidth)
        val longestWidth = split.maxOf { this.font.width(it) }

        return ScreenRectangle(
            ScreenPosition(
                (this.x - (longestWidth / 2f)).toInt(),
                (this.y - (this.font.lineHeight * split.size) / 2f).toInt()
            ), longestWidth, split.size * this.font.lineHeight
        )
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        val bounds = this.getBounds(graphics.width, graphics.height)
        val isHovered = bounds.containsPoint(mouseX, mouseY)
        val split = this.font.split(this.text.copy().withStyle {
            if (isHovered)
                it.withUnderlined(true)
            else it
        }, this.maxWidth)

        val yStart = bounds.top()
        for ((index, text) in split.withIndex()) {
            graphics.centeredText(this.font, text, this.x, yStart + (index * this.font.lineHeight).toFloat(), (if (isHovered) this.hoverColor else this.color).multiplyAlpha(this.opacity), true)
        }

//        graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, -1)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.onClick()
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
