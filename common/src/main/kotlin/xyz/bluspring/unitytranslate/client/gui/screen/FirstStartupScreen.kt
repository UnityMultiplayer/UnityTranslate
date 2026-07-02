package xyz.bluspring.unitytranslate.client.gui.screen

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.withAlpha
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.LogoTransitionOverlay
import xyz.bluspring.unitytranslate.client.gui.screen.intro.FirstTimeIntroSequence
import xyz.bluspring.unitytranslate.client.gui.screen.intro.IntroSequence
import xyz.bluspring.unitytranslate.client.gui.screen.intro.LangSelectIntroSequence

class FirstStartupScreen : UTScreen() {
    val sequence = listOf(
        FirstTimeIntroSequence(this),
        LangSelectIntroSequence(this),
    )

    var current = 0
    private val currentSequence: IntroSequence
        get() = this.sequence[this.current]
    private var transitioningSequence: IntroSequence? = null

    fun next() {
        this.currentSequence.reverse()
        this.transitioningSequence = this.currentSequence

        if (this.current++ >= this.sequence.size) {
            ClientPlatformProxy.instance.setScreen(null)
            return
        }

        if (!this.currentSequence.isActive) {
            this.next()
        } else {
            this.currentSequence.reset()
        }
    }

    override fun tick() {
        super.tick()
        if (this.transitioningSequence != null) {
            this.transitioningSequence!!.tick()

            if (this.transitioningSequence!!.isDone)
                this.transitioningSequence = null
        } else {
            this.currentSequence.tick()
        }
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        graphics.fill(0f, 0f, graphics.width.toFloat(), graphics.height.toFloat(),
            LogoTransitionOverlay.TOP_GRADIENT.withAlpha(1f), LogoTransitionOverlay.BOTTOM_GRADIENT.withAlpha(1f))

        if (this.transitioningSequence != null) {
            this.transitioningSequence!!.submit(graphics, partialTick, mouseX, mouseY)
        } else {
            this.currentSequence.submit(graphics, partialTick, mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.currentSequence.mouseClicked(mouseX, mouseY, button))
            return true

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.currentSequence.mouseReleased(mouseX, mouseY, button))
            return true

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (this.currentSequence.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        if (this.currentSequence.keyPressed(key, scanCode, modifiers))
            return true

        return super.keyPressed(key, scanCode, modifiers)
    }

    override fun keyReleased(key: Int, scanCode: Int, modifiers: Int): Boolean {
        if (this.currentSequence.keyReleased(key, scanCode, modifiers))
            return true

        return super.keyReleased(key, scanCode, modifiers)
    }

    override fun charTyped(codepoint: Int): Boolean {
        if (this.currentSequence.charTyped(codepoint))
            return true

        return super.charTyped(codepoint)
    }
}
