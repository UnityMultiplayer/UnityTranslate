package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper

open class PlainUIButton(
    private val font: FontReference,
    private val text: Component,

    private val x: (Int, Int) -> Float,
    private val y: (Int, Int) -> Float,
    private val maxWidth: Int = 10000,
    open var color: Int = ARGBHelper.color(255, 210, 210, 210),
    protected val hoverColor: Int = -1,
    private val onClick: () -> Unit,
) : UIElement() {
    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        val split = this.font.split(this.text, this.maxWidth)
        val longestWidth = split.maxOf { this.font.width(it) }

        return ScreenRectangle(
            ScreenPosition(
                (this.x(screenWidth, screenHeight) - (longestWidth / 2f)).toInt(),
                (this.y(screenWidth, screenHeight) - (this.font.lineHeight * split.size) / 2f).toInt()
            ), longestWidth, split.size * this.font.lineHeight
        )
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        val bounds = this.bounds(graphics.width, graphics.height)
        val isHovered = bounds.containsPoint(mouseX, mouseY)
        val split = this.font.split(this.text.copy().withStyle {
            if (isHovered)
                it.withUnderlined(true)
            else it
        }, this.maxWidth)

        val yStart = bounds.top()
        for ((index, text) in split.withIndex()) {
            graphics.centeredText(this.font, text, this.x(graphics.width, graphics.height), yStart + (index * this.font.lineHeight).toFloat(), if (isHovered) this.hoverColor else this.color, true)
        }

//        graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, -1)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            if (button == 0) {
                this.onClick()
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
