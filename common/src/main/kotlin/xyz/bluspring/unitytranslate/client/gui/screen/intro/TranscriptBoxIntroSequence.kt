package xyz.bluspring.unitytranslate.client.gui.screen.intro

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen
import xyz.bluspring.unitytranslate.client.gui.screen.config.ConfigureTranscriptBoxesScreen

class TranscriptBoxIntroSequence(parent: FirstStartupScreen) : IntroSequence(parent) {
    private lateinit var screen: ConfigureTranscriptBoxesScreen

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.screen = this.addChild(ConfigureTranscriptBoxesScreen {
            this.parent.next()
        })
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float) {
        this.screen.introText.opacity = transitionProgress
    }
}
