package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

class ActionContextBoxElement(val text: Component, val onClick: () -> Unit) : ContextBoxElement() {
    override fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val font = ClientPlatformProxy.instance.defaultFont
        graphics.centeredText(font, text, this.x + (this.width / 2f), this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt()) && button == 0) {
            this.onClick()
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
