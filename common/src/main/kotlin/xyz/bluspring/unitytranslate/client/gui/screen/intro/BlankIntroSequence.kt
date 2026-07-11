package xyz.bluspring.unitytranslate.client.gui.screen.intro

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen

class BlankIntroSequence(parent: FirstStartupScreen) : IntroSequence(parent) {
    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float) {
    }
}
