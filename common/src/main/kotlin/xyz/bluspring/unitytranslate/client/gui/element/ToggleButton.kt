package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import kotlin.reflect.KMutableProperty

class ToggleButton(
    val x: Float, val y: Float,
    val size: Float,

    val property: KMutableProperty<Boolean>,
    val outlineColor: ColorConfig,
    val outlineFocusedColor: ColorConfig,
    val disabledFillColor: ColorConfig,
    val enabledFillColor: ColorConfig,
) : UIElement(), FocusableUIElement {
    override var isFocused = false

    override fun bounds(
        screenWidth: Int,
        screenHeight: Int
    ): ScreenRectangle {
        return ScreenRectangle(this.x.toInt(), this.y.toInt(), this.size.toInt(), this.size.toInt())
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        val outlineMatrix = ColorConfig.separateMatrix(if (this.isFocused)
            this.outlineFocusedColor
        else this.outlineColor)
        graphics.outline(this.x, this.y, this.x + this.size, this.y + size, 1f,
            outlineMatrix.topLeft, outlineMatrix.topRight,
            outlineMatrix.bottomLeft, outlineMatrix.bottomRight
        )

        val fillMatrix = ColorConfig.separateMatrix(
            if (this.property.getter.call())
                this.enabledFillColor
            else
                this.disabledFillColor
        )

        graphics.fill(this.x + 2, this.y + 2, this.x + this.size - 2, this.y + this.size - 2,
            fillMatrix.topLeft, fillMatrix.topRight,
            fillMatrix.bottomLeft, fillMatrix.bottomRight
        )

        this.isFocused = this.bounds().containsPoint(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            this.property.setter.call(!this.property.getter.call())
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
