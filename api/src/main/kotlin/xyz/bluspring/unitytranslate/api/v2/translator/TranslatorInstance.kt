package xyz.bluspring.unitytranslate.api.v2.translator

import xyz.bluspring.unitytranslate.library.util.LangPair

abstract class TranslatorInstance {
    abstract fun supportsLanguage(langPair: LangPair): Boolean
    abstract suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String>
    open val isAvailable: Boolean = true
}
