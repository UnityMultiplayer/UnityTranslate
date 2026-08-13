package xyz.bluspring.unitytranslate.api.v2.display

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.display.text.Style
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent

abstract class LanguageDisplay {
    abstract val codec: MapCodec<out LanguageDisplay>
    abstract fun text(language: Language, style: Style): TextComponent

    companion object {
        @JvmField val CODEC: Codec<LanguageDisplay> = Codec.STRING.dispatch("type", UnityTranslateApi.instance::getLanguageDisplayId, UnityTranslateApi.instance::getLanguageDisplay)
    }
}
