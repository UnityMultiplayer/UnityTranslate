package xyz.bluspring.unitytranslate.client.renderer

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.animation.AnimationStrategy
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMouse
import java.awt.Color
import kotlin.math.floor
import kotlin.reflect.KMutableProperty0

object UnityTranslateElementaGui {
    val window = Window(ElementaVersion.V11)

    fun render(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float): Boolean {
        // Render after
        this.window.draw(matrixStack)

        return true
    }

    fun mouseClick(mouseX: Double, mouseY: Double, button: Int) {
        val (adjustedMouseX, adjustedMouseY) =
            if (mouseX == floor(mouseX) && mouseY == floor(mouseY)) {
                val x = UMouse.Scaled.x
                val y = UMouse.Scaled.y

                mouseX + (x - floor(x)) to mouseY + (y - floor(y))
            } else {
                mouseX to mouseY
            }

        this.window.mouseClick(adjustedMouseX, adjustedMouseY, button)
    }

    fun mouseReleased() {
        this.window.mouseRelease()
    }

    fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double) {
        this.window.mouseScroll(scrollX, scrollY)
    }

    fun keyPress(keyCode: Int, char: Char, modifiers: UKeyboard.Modifiers?) {
        this.window.keyType(char, keyCode)
    }

    fun update(width: Int, height: Int) {
        this.window.onWindowResize()
        UKeyboard.allowRepeatEvents(true)
    }

    fun KMutableProperty0<Int>.animate(strategy: AnimationStrategy, time: Float, newValue: Int, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Float>.animate(strategy: AnimationStrategy, time: Float, newValue: Float, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Long>.animate(strategy: AnimationStrategy, time: Float, newValue: Long, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Double>.animate(strategy: AnimationStrategy, time: Float, newValue: Double, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<Color>.animate(strategy: AnimationStrategy, time: Float, newValue: Color, delay: Float = 0f) {
        window.apply { this@animate.animate(strategy, time, newValue, delay) }
    }

    fun KMutableProperty0<*>.stopAnimating() {
        window.apply { this@stopAnimating.stopAnimating() }
    }
}