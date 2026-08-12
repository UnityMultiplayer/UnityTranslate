package xyz.bluspring.unitytranslate.api.v2.client.gui.element

import net.minecraft.network.chat.FormattedText
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.client.theme.ThemeConfig
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.multiplyAlpha

open class UILabel(
    val x: Float, val y: Float,
    open val text: FormattedText,
    val font: FontReference,

    val dropShadow: Boolean = true,
    open var color: Int = ThemeConfig.textColor,
    val alignX: HorizontalAlign = HorizontalAlign.LEFT,
    val alignY: VerticalAlign = VerticalAlign.CENTER,
    val maxWidth: Int = 10000,
) : UIElement(), FadeableUIElement {
    override var opacity = 1f

    override fun bounds(
        screenWidth: Int,
        screenHeight: Int
    ): ScreenRectangle {
        val split = this.font.split(this.text, this.maxWidth)
        val fontWidth = split.maxOf { this.font.width(it) }
        val textHeight = this.font.lineHeight * split.size

        return ScreenRectangle((this.x - this.alignX.adjustment(fontWidth)).toInt(), (this.y - this.alignY.adjustment(textHeight)).toInt(), fontWidth, textHeight)
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val split = this.font.split(this.text, this.maxWidth)
        val textHeight = this.font.lineHeight * split.size

        for ((index, text) in split.withIndex()) {
            val fontWidth = this.font.width(text)
            graphics.text(this.font, text, this.x - this.alignX.adjustment(fontWidth), this.y - this.alignY.adjustment(textHeight) + (index * this.font.lineHeight), this.color.multiplyAlpha(this.opacity), this.dropShadow)
        }

        super.submit(graphics, partialTick, mouseX, mouseY)
    }
}
