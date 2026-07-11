package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.locale.Language
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.multiplyAlpha
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

class UILabel(
    val x: Float, val y: Float,
    val text: FormattedCharSequence,
    val font: FontReference,

    val dropShadow: Boolean = true,
    var color: Int = ThemeConfig.textColor,
    val alignX: HorizontalAlign = HorizontalAlign.LEFT,
    val alignY: VerticalAlign = VerticalAlign.CENTER,
) : UIElement(), FadeableUIElement {
    override var opacity = 1f

    constructor(x: Float, y: Float, text: FormattedText, font: FontReference, dropShadow: Boolean = true, color: Int = -1, alignX: HorizontalAlign = HorizontalAlign.LEFT, alignY: VerticalAlign = VerticalAlign.CENTER)
        : this(x, y, Language.getInstance().getVisualOrder(text), font, dropShadow, color, alignX, alignY)

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
        val fontWidth = this.font.width(this.text)
        val fontHeight = this.font.lineHeight - 2
        return ScreenRectangle((this.x - this.alignX.adjustment(fontWidth)).toInt(), (this.y - this.alignY.adjustment(fontHeight)).toInt(), this.font.width(this.text), this.font.lineHeight)
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val fontWidth = this.font.width(this.text)
        val fontHeight = this.font.lineHeight - 2
        graphics.text(this.font, this.text, this.x - this.alignX.adjustment(fontWidth), this.y - this.alignY.adjustment(fontHeight), this.color.multiplyAlpha(this.opacity), this.dropShadow)

        super.submit(graphics, partialTick, mouseX, mouseY)
    }
}
