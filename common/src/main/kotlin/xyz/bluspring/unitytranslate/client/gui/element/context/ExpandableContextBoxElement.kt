package xyz.bluspring.unitytranslate.client.gui.element.context

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics

class ExpandableContextBoxElement(val key: String, val elements: Collection<ContextBoxElement>) : ContextBoxElement() {
    override fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
    }
}
