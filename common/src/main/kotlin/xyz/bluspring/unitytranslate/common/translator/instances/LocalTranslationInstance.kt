package xyz.bluspring.unitytranslate.common.translator.instances

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate

class LocalTranslationInstance(instance: UnityTranslate, val usesCuda: Boolean) : TranslationInstance(instance) {
    val library = instance.library

    override val supportedLanguages: Multimap<Language, Language>
        get() = HashMultimap.create()

    override fun supportsLanguage(from: Language, to: Language): Boolean {
        return library.packageIndex.getTranslationPackages(from.code, to.code).isNotEmpty()
    }

    override suspend fun batchTranslate(
        from: String,
        to: String,
        texts: List<String>
    ): List<String> {
        markTranslating()
        val translator = library.getTranslator(from, to, usesCuda)
        return translator.batchTranslate(texts).apply {
            unmarkTranslating()
        }
    }

    override suspend fun translate(from: String, to: String, text: String): String {
        markTranslating()
        val translator = library.getTranslator(from, to, usesCuda)
        return translator.batchTranslate(listOf(text)).first().apply {
            unmarkTranslating()
        }
    }
}