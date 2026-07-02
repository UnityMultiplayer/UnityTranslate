package xyz.bluspring.unitytranslate.api.v2.client.gui.font

import net.minecraft.client.gui.Font
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence
import xyz.bluspring.unitytranslate.client.renderer.ui.font.MinecraftFontReference

/**
 * A reference to the current font used.
 */
interface FontReference {
    val lineHeight: Int

    fun width(text: FormattedText): Int
    fun width(text: FormattedCharSequence): Int
    fun width(text: String): Int
    fun split(text: FormattedText, maxWidth: Int): List<FormattedCharSequence>

    companion object {
        @JvmStatic
        fun minecraft(font: Font): FontReference = MinecraftFontReference(font) as FontReference
    }
}
