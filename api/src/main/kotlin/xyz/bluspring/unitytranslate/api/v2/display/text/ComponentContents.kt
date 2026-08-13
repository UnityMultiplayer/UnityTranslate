package xyz.bluspring.unitytranslate.api.v2.display.text

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi

sealed interface ComponentContents {
    val text: String

    @JvmRecord
    data class Literal(override val text: String) : ComponentContents

    @JvmRecord
    data class Translatable(val key: String, val args: List<Any?>) : ComponentContents {
        override val text: String
            get() = UnityTranslateApi.instance.platform.translated(key, *this.args.toTypedArray())
    }
}
