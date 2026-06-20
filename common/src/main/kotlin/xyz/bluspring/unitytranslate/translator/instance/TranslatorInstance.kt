package xyz.bluspring.unitytranslate.translator.instance

import xyz.bluspring.unitytranslate.library.util.LangPair

abstract class TranslatorInstance(val id: String) {
    abstract fun supportsLanguage(langPair: LangPair): Boolean
    abstract suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String>
    open val isAvailable: Boolean = true
}
