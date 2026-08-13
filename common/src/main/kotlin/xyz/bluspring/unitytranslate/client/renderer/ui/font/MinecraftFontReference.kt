package xyz.bluspring.unitytranslate.client.renderer.ui.font

import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Style
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.util.PlatformConversion.asMinecraft
import xyz.bluspring.unitytranslate.util.PlatformConversion.asUnityTranslate

data class MinecraftFontReference(val font: Font) : FontReference {
    override val lineHeight: Int
        get() = this.font.lineHeight

    override fun width(text: TextComponent): Int = this.font.width(text.asMinecraft())
    override fun width(text: String): Int = this.font.width(text)
    override fun split(text: TextComponent, maxWidth: Int): List<TextComponent> {
        return this.font.splitter.splitLines(text.asMinecraft(), maxWidth, Style.EMPTY)
            .map { it.asUnityTranslate() }
    }
    override fun substr(text: TextComponent, maxWidth: Int): TextComponent = this.font.substrByWidth(text.asMinecraft(), maxWidth).asUnityTranslate()
}
