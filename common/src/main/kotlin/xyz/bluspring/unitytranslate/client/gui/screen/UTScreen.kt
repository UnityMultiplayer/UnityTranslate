package xyz.bluspring.unitytranslate.client.gui.screen

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics

abstract class UTScreen {
    abstract fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int)
}
