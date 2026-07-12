package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.network.chat.FormattedText
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

open class PlainUIButton(
    x: Float,
    y: Float,
    text: FormattedText,
    font: FontReference,

    dropShadow: Boolean = true,
    color: Int = ThemeConfig.plainButton,
    protected val hoverColor: Int = ThemeConfig.plainButtonHover,
    protected val disabledColor: Int = ThemeConfig.plainButtonDisabled,
    alignX: HorizontalAlign = HorizontalAlign.CENTER,
    alignY: VerticalAlign = VerticalAlign.BOTTOM,
    maxWidth: Int = 10000,

    private val onClick: () -> Unit,
) : UILabel(x, y, text, font, dropShadow, color, alignX, alignY, maxWidth), FocusableUIElement {
    override var isFocused: Boolean = false
    var isDisabled = false

    override var color: Int
        get() {
            return if (this.isDisabled)
                this.disabledColor
            else if (this.isFocused)
                this.hoverColor
            else
                super.color
        }
        set(value) {
            super.color = value
        }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val bounds = this.getBounds(graphics.width, graphics.height)
        val isHovered = bounds.containsPoint(mouseX, mouseY)

        super.submit(graphics, partialTick, mouseX, mouseY)

        this.isFocused = isHovered
//        graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, -1)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !this.isDisabled) {
                this.onClick()
                return true
            }
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
