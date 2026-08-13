package xyz.bluspring.unitytranslate.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.TextColor
import xyz.bluspring.unitytranslate.api.v2.display.text.MutableTextComponent
import xyz.bluspring.unitytranslate.api.v2.display.text.Style
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.opaque
import xyz.bluspring.unitytranslate.api.v2.util.CacheUtils.buildOrCached
import java.util.*
import net.minecraft.network.chat.Style as MCStyle

object PlatformConversion {
    fun Style.asMinecraft(): MCStyle = buildOrCached {
        return MCStyle.EMPTY
            .withColor(this.color ?: -1)
            .withShadowColor(this.shadow ?: 0)
            .withBold(this.bold)
            .withItalic(this.italic)
            .withUnderlined(this.underlined)
            .withStrikethrough(this.strikethrough)
            .withObfuscated(this.obfuscated)
    }

    fun MCStyle.asUnityTranslate(): Style = buildOrCached {
        if (this.isEmpty)
            return Style.EMPTY

        return Style(
            this.color?.value,
            this.shadowColor,
            this.isBold,
            this.isItalic,
            this.isUnderlined,
            this.isStrikethrough,
            this.isObfuscated,
        )
    }

    fun TextComponent.asMinecraft(): Component = buildOrCached {
        val mainText = Component.empty()

        this.visit({ text, style ->
            mainText.append(Component.literal(text).withStyle(style.asMinecraft()))
        })

        return mainText
    }

    fun FormattedText.asUnityTranslate(): TextComponent = buildOrCached {
        val mainText = TextComponent.empty()

        this.visit({ style, text ->
            mainText.append(TextComponent.literal(text).withStyle(style.asUnityTranslate()))

            Optional.empty()
        }, MCStyle.EMPTY)

        return mainText
    }

    fun MutableTextComponent.withStyle(color: ChatFormatting): MutableTextComponent
        = this.withStyle(TextColor.fromLegacyFormat(color)!!)

    fun MutableTextComponent.withStyle(color: TextColor): MutableTextComponent
        = this.withStyle { it.withColor(color) }

    fun Style.withColor(color: ChatFormatting): Style
        = this.withColor(TextColor.fromLegacyFormat(color)!!)

    fun Style.withColor(color: TextColor): Style
        = this.withColor(color.value.opaque)
}
