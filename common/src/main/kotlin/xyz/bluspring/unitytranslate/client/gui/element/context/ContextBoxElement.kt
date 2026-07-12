package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.gui.element.FocusableUIElement
import xyz.bluspring.unitytranslate.client.gui.element.UIElement
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

abstract class ContextBoxElement : UIElement(), FocusableUIElement {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    open val height: Float = 20f

    override var isFocused: Boolean = false

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return ScreenRectangle(this.x.toInt(), this.y.toInt(), this.width.toInt(), this.height.toInt())
    }

    final override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val bounds = this.bounds()

        val bgMatrix = ColorConfig.separateMatrix(if (this.isFocused) {
            ThemeConfig.contextBoxElementBackgroundFocused
        } else {
            ThemeConfig.contextBoxElementBackground
        })

        val outlineMatrix = ColorConfig.separateMatrix(if (this.isFocused) {
            ThemeConfig.contextBoxElementOutlineFocused
        } else {
            ThemeConfig.contextBoxElementOutline
        })

        graphics.fill(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), bgMatrix)
        graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, outlineMatrix)

        super.submit(graphics, partialTick, mouseX, mouseY)

        graphics.enableScissor(bounds.left(), bounds.top(), bounds.width, bounds.height)
        this.submitElement(graphics, partialTick, mouseX, mouseY)
        graphics.disableScissor()
    }

    protected abstract fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int)
}
