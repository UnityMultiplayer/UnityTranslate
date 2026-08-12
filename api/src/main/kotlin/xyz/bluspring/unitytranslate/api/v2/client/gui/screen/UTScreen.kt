package xyz.bluspring.unitytranslate.api.v2.client.gui.screen

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIElement
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle

abstract class UTScreen : UIElement() {
    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle = ScreenRectangle(0, 0, screenWidth, screenHeight)

    open fun shouldCloseOnEsc(): Boolean = true

    open fun onExit() {
        UnityTranslateApi.instance.client.setScreen(null)
    }
}
