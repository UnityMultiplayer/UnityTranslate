package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import kotlin.math.round
import kotlin.reflect.KMutableProperty
import kotlin.reflect.typeOf

class SliderElement<T : Number>(
    val x: Float, val y: Float,
    val width: Float, val height: Float,

    val property: KMutableProperty<T>,
    min: T, max: T, step: T,
    val font: FontReference,
    val visualizer: (T) -> Component,
) : UIElement() {
    private val min = min.toFloat()
    private val max = max.toFloat()
    private val step = step.toFloat()

    var value: Float
        get() = this.property.getter.call().toFloat()
        set(value) = this.property.setter.call(when (this.property.getter.returnType) {
            typeOf<Int>() -> value.toInt()
            typeOf<Float>() -> value
            typeOf<Double>() -> value.toDouble()
            typeOf<Byte>() -> value.toInt().toByte()
            typeOf<Short>() -> value.toInt().toShort()
            typeOf<Long>() -> value.toLong()
            else -> throw IllegalArgumentException("Unsupported number type ${this.property.getter.returnType}!")
        })

    private val normalizedValue: Float
        get() {
            val value = this.value
            val range = this.max - this.min
            return (value - this.min) / range
        }

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return ScreenRectangle(this.x.toInt(), this.y.toInt(), this.width.toInt(), this.height.toInt())
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        if (this.isSelected) {
            this.value = this.calculateCurrentValue(mouseX)
        }

        val normalized = this.normalizedValue
        graphics.fill(this.x, this.y + (this.height / 2f), this.x + this.width, this.y + (this.height / 2f) + 1, -1)
        graphics.fill(this.x - 1 + (this.width * normalized), this.y + 1, this.x + (this.width * normalized) + 1, this.y + this.height, -1)

        graphics.text(this.font, this.visualizer(this.property.getter.call()), this.x + this.width + 3, this.y + (this.height / 2f) - 3, -1, false)
    }

    private fun calculateCurrentValue(mouseX: Int): Float {
        val step = this.step / (this.max.minus(this.min))
        val delta = Mth.clamp((mouseX - this.x) / this.width, 0f, 1f)
        return this.min + (round(delta / step) * step) * (this.max - this.min)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            this.value = this.calculateCurrentValue(mouseX.toInt())
            this.isSelected = true
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.isSelected) {
            this.isSelected = false
            return true
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }
}
