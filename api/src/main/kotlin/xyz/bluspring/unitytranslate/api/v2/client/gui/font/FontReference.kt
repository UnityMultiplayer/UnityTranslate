package xyz.bluspring.unitytranslate.api.v2.client.gui.font

import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

/**
 * A reference to the current font used.
 */
interface FontReference {
    fun width(text: FormattedText): Int
    fun width(text: FormattedCharSequence): Int
    fun width(text: String): Int
}
