package xyz.bluspring.unitytranslate.client.gui.screen.intro

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.DropdownList
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen

class LangSelectIntroSequence(parent: FirstStartupScreen) : IntroSequence(parent) {


    override fun init(width: Int, height: Int) {
        super.init(width, height)

        this.addChild(DropdownList(width / 2f, height / 2f, 140f, 20f, ClientPlatformProxy.instance.defaultFont, listOf()))
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float) {
    }
}
