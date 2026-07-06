package xyz.bluspring.unitytranslate.translator.instance.google

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.util.LangPair

object GoogleCloudTranslatorInstance : TranslatorInstance() {
    override suspend fun getSupportedLanguages(): Set<Language> {
        TODO("Not yet implemented")
    }

    override suspend fun supportsLanguage(langPair: LangPair): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun batchTranslate(
        text: List<String>,
        langPair: LangPair
    ): List<String> {
        TODO("Not yet implemented")
    }
}
