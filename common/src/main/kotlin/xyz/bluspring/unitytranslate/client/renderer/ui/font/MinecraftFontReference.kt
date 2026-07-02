package xyz.bluspring.unitytranslate.client.renderer.ui.font

import net.minecraft.client.gui.Font
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference

data class MinecraftFontReference(val font: Font) : FontReference {
    override val lineHeight: Int
        get() = this.font.lineHeight

    override fun width(text: FormattedText): Int = this.font.width(text)
    override fun width(text: String): Int = this.font.width(text)
    override fun width(text: FormattedCharSequence): Int = this.font.width(text)
    override fun split(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> = this.font.split(text, maxWidth)
}
