package xyz.bluspring.unitytranslate.api.v2.client.gui.font

import net.minecraft.client.gui.Font
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

data class MinecraftFontReference(val font: Font) : FontReference {
    override fun width(text: FormattedText): Int = this.font.width(text)
    override fun width(text: String): Int = this.font.width(text)
    override fun width(text: FormattedCharSequence): Int = this.font.width(text)
}
