package xyz.bluspring.unitytranslate.transcriber.display

import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.display.LanguageDisplay
import xyz.bluspring.unitytranslate.api.v2.display.text.Style
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent

object BuiltinLanguageDisplays {
    object None : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<None> = MapCodec.unit(None)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        override fun text(language: Language, style: Style): TextComponent = TextComponent.empty()
    }

    object LangCodeShort : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangCodeShort> = MapCodec.unit(LangCodeShort)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript en
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.languageCode).withStyle(style)
    }

    object LangCodeShortUppercase : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangCodeShortUppercase> = MapCodec.unit(LangCodeShortUppercase)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript EN
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.languageCode.uppercase()).withStyle(style)
    }

    object LangCodeLong : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangCodeLong> = MapCodec.unit(LangCodeLong)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript en-US
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.formatted).withStyle(style)
    }

    object LangCodeLongUppercase : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangCodeLongUppercase> = MapCodec.unit(LangCodeLongUppercase)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript EN-US
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.formatted.uppercase()).withStyle(style)
    }

    object LangNameLocalized : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangNameLocalized> = MapCodec.unit(LangNameLocalized)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript English, US
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.localizedText).withStyle(style)
    }

    object LangNameNative : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangNameNative> = MapCodec.unit(LangNameNative)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript Español, Castellano
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.nativeText).withStyle(style)
    }

    object LangNameLocalizedShort : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangNameLocalizedShort> = MapCodec.unit(LangNameLocalizedShort)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript English
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.localizedShortText).withStyle(style)
    }

    object LangNameNativeShort : LanguageDisplay() {
        @JvmField val CODEC: MapCodec<LangNameNativeShort> = MapCodec.unit(LangNameNativeShort)
        override val codec: MapCodec<out LanguageDisplay> = CODEC

        // Transcript Español, Castellano
        override fun text(language: Language, style: Style): TextComponent = TextComponent.literal(language.nativeShortText).withStyle(style)
    }
}
