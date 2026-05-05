package gg.essential.universal.utils

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.util.FormattedCharSink

private class TextBuilder(private val isFormatted: Boolean) : FormattedCharSink {
    private val builder = StringBuilder()
    private var cachedStyle: Style? = null

    override fun accept(index: Int, style: Style, codePoint: Int): Boolean  {
        if (isFormatted && style != cachedStyle) {
            cachedStyle = style
            builder.append(formatString(style))
        }

        builder.append(codePoint.toChar())
        return true
    }

    fun getString() = builder.toString()

    private fun formatString(style: Style): String {
        val builder = StringBuilder("§r")

        style.color?.let(colorToFormatChar::get)?.let {
            builder.append(it)
        }

        when {
            style.isBold -> builder.append("§l")
            style.isItalic -> builder.append("§o")
            style.isUnderlined -> builder.append("§n")
            style.isStrikethrough -> builder.append("§m")
            style.isObfuscated -> builder.append("§k")
        }

        return builder.toString()
    }

    companion object {
        private val colorToFormatChar = ChatFormatting.values().mapNotNull { format ->
            TextColor.fromLegacyFormat(format)?.let { it to format }
        }.toMap()
    }
}

fun Component.toUnformattedString(): String {
    val builder = TextBuilder(false)
    visualOrderText.accept(builder)
    return builder.getString()
}

fun Component.toFormattedString(): String {
    val builder = TextBuilder(true)
    visualOrderText.accept(builder)
    return builder.getString()
}
