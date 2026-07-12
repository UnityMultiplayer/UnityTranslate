package xyz.bluspring.unitytranslate.client.gui.element.context

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig
import kotlin.reflect.KMutableProperty

class ToggleContextBoxElement(
    val property: KMutableProperty<Boolean>,
    val rootKey: String,
) : ContextBoxElement() {
    override fun submitElement(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val font = ClientPlatformProxy.instance.defaultFont
        val value = this.property.getter.call()
        val text = Component.translatable(this.rootKey).append(": ").append(
            Component.translatable("unitytranslate.value.$value")
                .withStyle { style -> style.withColor(if (value) ThemeConfig.enabledText else ThemeConfig.disabledText) }
        )
        graphics.centeredText(font, text, this.x + (this.width / 2f), this.y + (this.bounds().height / 2f) - (font.lineHeight / 2f),
            if (this.isFocused) ThemeConfig.contextBoxElementTextFocused else ThemeConfig.contextBoxElementText, true)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.bounds().containsPoint(mouseX.toInt(), mouseY.toInt())) {
            if (button == 0 || button == 1) {
                this.property.setter.call(!this.property.getter.call())
            }

            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }
}
