package xyz.bluspring.unitytranslate.api.v2.display

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi

abstract class LanguageDisplay {
    abstract val codec: MapCodec<out LanguageDisplay>
    abstract fun text(language: Language, style: Style): Component

    companion object {
        @JvmField val CODEC: Codec<LanguageDisplay> = Codec.STRING.dispatch("type", UnityTranslateApi.instance::getLanguageDisplayId, UnityTranslateApi.instance::getLanguageDisplay)
    }
}
