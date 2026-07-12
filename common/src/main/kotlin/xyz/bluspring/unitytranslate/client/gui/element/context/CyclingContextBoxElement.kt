package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.config.NameProvidingEntry
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig
import kotlin.reflect.KMutableProperty

class CyclingContextBoxElement<E : NameProvidingEntry>(
    elements: Collection<E>,
    val property: KMutableProperty<E>,
    val rootKey: String,
) : ContextBoxElement() {
    val elements = elements.toList().sorted()
    var currentIndex = this.elements.indexOf(this.property.getter.call())

    override fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val font = ClientPlatformProxy.instance.defaultFont
        val element = this.elements[this.currentIndex]
        val text = Component.translatable(this.rootKey)
            .append(": ")
            .append(Component.translatable("${this.rootKey}.${element.serializedName}"))
        graphics.centeredText(font, text, this.x + (this.width / 2f), this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            this.currentIndex += if (button == 0) 1 else if (button == 1) -1 else 0
            if (this.currentIndex < 0)
                this.currentIndex = this.elements.lastIndex
            else if (this.currentIndex >= this.elements.size)
                this.currentIndex = 0

            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
