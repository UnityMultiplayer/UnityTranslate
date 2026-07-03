package xyz.bluspring.unitytranslate.translator.instance

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.util.LangPair

object InactiveTranslatorInstance : TranslatorInstance() {
    override suspend fun getSupportedLanguages(): Set<Language> = emptySet()
    override suspend fun supportsLanguage(langPair: LangPair): Boolean = true
    override suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String> = text
    override suspend fun isAvailable(): Boolean = false
}
