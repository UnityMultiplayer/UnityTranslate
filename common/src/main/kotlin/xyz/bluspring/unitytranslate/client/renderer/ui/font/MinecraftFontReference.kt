package xyz.bluspring.unitytranslate.client.renderer.ui.font

import net.minecraft.client.gui.Font
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent

data class MinecraftFontReference(val font: Font) : FontReference {
    override val lineHeight: Int
        get() = this.font.lineHeight

    override fun width(text: TextComponent): Int = this.font.width(text)
    override fun width(text: String): Int = this.font.width(text)
    override fun split(text: TextComponent, maxWidth: Int): List<TextComponent> = this.font.split(text, maxWidth)
    override fun substr(text: TextComponent, maxWidth: Int): TextComponent = this.font.substrByWidth(text, maxWidth)
}
