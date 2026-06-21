package xyz.bluspring.unitytranslate.translator.instance

import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.library.util.LangPair

object InactiveTranslatorInstance : TranslatorInstance() {
    override suspend fun supportsLanguage(langPair: LangPair): Boolean = true
    override suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String> = text
}
