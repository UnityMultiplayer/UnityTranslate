package xyz.bluspring.unitytranslate.client.gui.screen

import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.UIElement

abstract class UTScreen : UIElement() {
    override val bounds: ScreenRectangle
        get() = ScreenRectangle(ScreenPosition(0, 0), ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
}
