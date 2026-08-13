package xyz.bluspring.unitytranslate.api.v2.client.gui.element.context

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.theme.ThemeConfig
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent

class ExpandableContextBoxElement(val text: TextComponent, val elements: Collection<ContextBoxElement>) : ContextBoxElement() {
    private var contextBox: ContextBox? = null

    var elementWidth = this.width

    override var isFocused: Boolean
        get() = super.isFocused || (this.contextBox != null && this.contextBox!!.isFocused)
        set(value) {
            super.isFocused = value
        }

    override fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val font = UnityTranslateApi.instance.client.defaultFont
        graphics.centeredText(font, text, this.x + (this.width / 2f), this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)

        val arrowText = TextComponent.literal("➤")
        graphics.text(font, arrowText, this.x + this.width - font.width(arrowText) - 1, this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)

        if (this.isFocused && this.contextBox == null) {
            this.contextBox = ContextBox(this.x + this.width, this.y, this.elementWidth.toInt(), this.elements, false)
            this.addChild(this.contextBox!!)
            this.contextBox!!.setup(UnityTranslateApi.instance.client.viewportWidth, UnityTranslateApi.instance.client.viewportHeight)
        } else if (!this.isFocused && this.contextBox != null) {
            this.removeChild(this.contextBox!!)
            this.contextBox = null
        }
    }
}
