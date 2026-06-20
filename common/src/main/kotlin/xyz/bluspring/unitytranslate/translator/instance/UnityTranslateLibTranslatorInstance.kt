package xyz.bluspring.unitytranslate.translator.instance

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.library.UnityTranslateLib
import xyz.bluspring.unitytranslate.library.UnityTranslateLibInstance
import xyz.bluspring.unitytranslate.library.util.LangPair

object UnityTranslateLibTranslatorInstance : TranslatorInstance("unitytranslatelib") {
    val library = UnityTranslateLib(UnityTranslateApi.instance.storagePath.resolve("library"))
    val instances = mutableMapOf<LangPair, UnityTranslateLibInstance>()

    override val isAvailable: Boolean
        get() = UnityTranslateLib.isAvailable()

    override fun supportsLanguage(langPair: LangPair): Boolean {
        return true
    }

    override suspend fun batchTranslate(
        text: List<String>,
        langPair: LangPair
    ): List<String> {
        val instance = this.instances[langPair]!!
        return instance.batchTranslate(text)
    }
}
