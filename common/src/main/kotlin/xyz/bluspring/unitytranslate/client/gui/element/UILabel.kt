package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference

class UILabel(
    val x: Float, val y: Float,
    val text: Component,
    val font: FontReference,

    val dropShadow: Boolean = true,
    val color: Int = -1,
    val alignX: HorizontalAlign = HorizontalAlign.LEFT,
    val alignY: VerticalAlign = VerticalAlign.CENTER,
) : UIElement() {
    enum class HorizontalAlign(val adjustment: (Int) -> Float) {
        LEFT({ 0f }), CENTER({ it / 2f }), RIGHT({ it.toFloat() })
    }

    enum class VerticalAlign(val adjustment: (Int) -> Float) {
        LEFT({ 0f }), CENTER({ it / 2f }), RIGHT({ it.toFloat() })
    }

    override fun bounds(
        screenWidth: Int,
        screenHeight: Int
    ): ScreenRectangle {
        return ScreenRectangle(this.x.toInt(), this.y.toInt(), this.font.width(this.text), this.font.lineHeight)
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val fontWidth = this.font.width(this.text)
        val fontHeight = this.font.lineHeight - 2
        graphics.text(this.font, this.text, this.x - this.alignX.adjustment(fontWidth), this.y - this.alignY.adjustment(fontHeight), this.color, this.dropShadow)

        super.submit(graphics, partialTick, mouseX, mouseY)
    }
}
