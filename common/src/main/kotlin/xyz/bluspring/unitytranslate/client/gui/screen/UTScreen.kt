package xyz.bluspring.unitytranslate.client.gui.screen

import net.minecraft.client.gui.navigation.ScreenPosition
import net.minecraft.client.gui.navigation.ScreenRectangle
import xyz.bluspring.unitytranslate.client.gui.element.UIElement

abstract class UTScreen : UIElement() {
    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle = ScreenRectangle(ScreenPosition(0, 0), screenWidth, screenHeight)
}
