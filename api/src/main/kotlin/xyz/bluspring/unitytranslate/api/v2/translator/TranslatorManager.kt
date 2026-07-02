package xyz.bluspring.unitytranslate.api.v2.translator

import kotlinx.coroutines.Deferred
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.util.LangPair

interface TranslatorManager {
    val instances: Collection<TranslatorInstance>

    fun getInstanceById(id: String): TranslatorInstance?

    fun queue(text: String, fromLang: Language, toLang: Language): Deferred<String> = queue(text, LangPair(fromLang, toLang))
    fun queue(text: String, langPair: LangPair): Deferred<String>
}
