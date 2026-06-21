package xyz.bluspring.unitytranslate.api.v2.translator

import kotlinx.coroutines.Deferred
import xyz.bluspring.unitytranslate.library.util.LangPair

interface TranslatorManager {
    val instances: Collection<TranslatorInstance>

    fun getInstanceById(id: String): TranslatorInstance?

    fun queue(text: String, fromLang: String, toLang: String): Deferred<String> = queue(text, LangPair(fromLang, toLang))
    fun queue(text: String, langPair: LangPair): Deferred<String>
}
