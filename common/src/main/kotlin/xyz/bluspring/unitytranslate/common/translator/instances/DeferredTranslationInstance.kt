package xyz.bluspring.unitytranslate.common.translator.instances

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate

abstract class DeferredTranslationInstance(instance: UnityTranslate) : TranslationInstance(instance) {
    override val supportedLanguages: Multimap<Language, Language>
        get() = HashMultimap.create()

    override fun supportsLanguage(from: Language, to: Language): Boolean {
        return true
    }
}