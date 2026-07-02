package xyz.bluspring.unitytranslate.api.v2.translator

import xyz.bluspring.unitytranslate.api.v2.util.LangPair

abstract class TranslatorInstance {
    abstract suspend fun supportsLanguage(langPair: LangPair): Boolean
    abstract suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String>
    open suspend fun isAvailable(): Boolean = true
}
