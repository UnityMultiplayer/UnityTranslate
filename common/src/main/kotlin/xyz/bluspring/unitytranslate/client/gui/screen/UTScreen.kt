package xyz.bluspring.unitytranslate.client.gui.screen

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.UIElement

abstract class UTScreen : UIElement() {
    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle = ScreenRectangle(ScreenPosition(0, 0), screenWidth, screenHeight)

    open fun shouldCloseOnEsc(): Boolean = true

    open fun onExit() {
        ClientPlatformProxy.instance.setScreen(null)
    }

    override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        if (key == InputConstants.KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onExit()
            return true
        }

        return super.keyPressed(key, scanCode, modifiers)
    }
}
