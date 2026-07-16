package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

class ExpandableContextBoxElement(val text: Component, val elements: Collection<ContextBoxElement>) : ContextBoxElement() {
    private var contextBox: ContextBox? = null

    var elementWidth = this.width

    override var isFocused: Boolean
        get() = super.isFocused || (this.contextBox != null && this.contextBox!!.isFocused)
        set(value) {
            super.isFocused = value
        }

    override fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val font = ClientPlatformProxy.instance.defaultFont
        graphics.centeredText(font, text, this.x + (this.width / 2f), this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)

        val arrowText = Component.literal("➤")
        graphics.text(font, arrowText, this.x + this.width - font.width(arrowText) - 1, this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)

        if (this.isFocused && this.contextBox == null) {
            this.contextBox = ContextBox(this.x + this.width, this.y, this.elementWidth.toInt(), this.elements, false)
            this.addChild(this.contextBox!!)
            this.contextBox!!.setup(ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
        } else if (!this.isFocused && this.contextBox != null) {
            this.removeChild(this.contextBox!!)
            this.contextBox = null
        }
    }
}
