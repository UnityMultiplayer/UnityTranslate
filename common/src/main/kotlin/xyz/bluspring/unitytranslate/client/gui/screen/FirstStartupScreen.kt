package xyz.bluspring.unitytranslate.client.gui.screen

import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.multiplyAlpha
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.gui.screen.intro.*
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

class FirstStartupScreen : UTScreen() {
    val sequence = listOf(
        FirstTimeIntroSequence(this),
        LangSelectIntroSequence(this),
        DownloadIntroSequence(this),
    )

    var current = 0
    private val currentSequence: IntroSequence
        get() = this.sequence.getOrNull(this.current) ?: BlankIntroSequence(this)
    private var transitioningSequence: IntroSequence? = null
    private var exitTick = -1
    private val transitionTime = 24

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.currentSequence.setup(width, height)
    }

    fun back() {
        this.currentSequence.reverse()
        this.transitioningSequence = this.currentSequence

        if (--this.current < 0) {
            ClientPlatformProxy.instance.setScreen(null)
            return
        }

        if (!this.currentSequence.isActive) {
            this.back()
        } else {
            this.currentSequence.reset()
            this.currentSequence.setup(ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
        }
    }

    fun next() {
        this.currentSequence.reverse()
        this.transitioningSequence = this.currentSequence

        if (++this.current >= this.sequence.size) {
            this.exitTick = 0
            UnityTranslateClient.handledFirstJoin = true
            for (config in UnityTranslateApiImpl.allConfigs) {
                config.save()
            }

            return
        }

        if (!this.currentSequence.isActive) {
            this.next()
        } else {
            this.currentSequence.reset()
            this.currentSequence.setup(ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
        }
    }

    override fun tick() {
        super.tick()
        if (this.transitioningSequence != null) {
            this.transitioningSequence!!.tick()

            if (this.transitioningSequence!!.isDone)
                this.transitioningSequence = null
        } else {
            if (this.exitTick >= 0 && this.exitTick++ >= this.transitionTime) {
                ClientPlatformProxy.instance.setScreen(null)
            }

            this.currentSequence.tick()
        }
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val alpha = if (this.exitTick >= 0)
            1f - ((this.exitTick + partialTick) / this.transitionTime.toFloat()).coerceAtMost(1f)
        else 1f
        val matrix = ColorConfig.separateMatrix(ThemeConfig.mainBackground)
        graphics.fill(0f, 0f, graphics.width.toFloat(), graphics.height.toFloat(),
            matrix.topLeft.multiplyAlpha(alpha), matrix.topRight.multiplyAlpha(alpha),
            matrix.bottomLeft.multiplyAlpha(alpha), matrix.bottomRight.multiplyAlpha(alpha),
        )

        if (this.transitioningSequence != null) {
            this.transitioningSequence!!.submit(graphics, partialTick, -100, -100)
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
