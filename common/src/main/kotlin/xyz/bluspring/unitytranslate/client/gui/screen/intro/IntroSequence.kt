package xyz.bluspring.unitytranslate.client.gui.screen.intro

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.element.FadeableUIElement
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen

abstract class IntroSequence(protected val parent: FirstStartupScreen) : UIElement() {
    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle = ScreenRectangle(0, 0, screenWidth, screenHeight)

    private var currentTick = 0
    private val transitionTime = 24 // ticks

    protected var isReversed = false
        private set

    open val isActive: Boolean = true
    val isDone: Boolean
        get() = this.isReversed && this.currentTick <= 0

    open fun reset() {
        this.currentTick = 0
        this.isReversed = false
    }

    fun reverse() {
        this.isReversed = true
        this.currentTick = this.transitionTime
    }

    override fun tick() {
        super.tick()

        if (!this.isReversed) {
            if (this.currentTick >= this.transitionTime) {
                this.currentTick = this.transitionTime
            } else {
                this.currentTick++
            }
        } else {
            if (this.currentTick <= 0) {
                this.currentTick = 0
            } else {
                this.currentTick--
            }
        }
    }

    final override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val delta = ((if (this.isReversed)
            this.currentTick - partialTick
        else this.currentTick + partialTick) / transitionTime.toFloat()).coerceAtMost(1f)

        for (element in this.children) {
            if (element is FadeableUIElement) {
                element.opacity = delta
            }
        }

        this.submit(graphics, partialTick, mouseX, mouseY, delta)

        super.submit(graphics, partialTick, mouseX, mouseY)
    }

    protected abstract fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float)
}
