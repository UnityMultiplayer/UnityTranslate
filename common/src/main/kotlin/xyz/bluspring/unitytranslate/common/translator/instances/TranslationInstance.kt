package xyz.bluspring.unitytranslate.common.translator.instances

import com.google.common.collect.Multimap
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.Flags
import java.util.concurrent.atomic.AtomicInteger

abstract class TranslationInstance(val instance: UnityTranslate) {
    abstract val supportedLanguages: Multimap<Language, Language>
    val currentlyTranslating: Int
        get() = internalTranslationCount.get()

    private val internalTranslationCount = AtomicInteger(0)

    open fun supportsLanguage(from: Language, to: Language): Boolean {
        if (!supportedLanguages.containsKey(from)) {
            return false
        }

        val supportedTargets = supportedLanguages.get(from)

        return supportedTargets.contains(to)
    }

    open suspend fun batchTranslate(texts: List<String>, from: Language, to: Language): List<String>? {
        if (!supportsLanguage(from, to))
            return null

        return try {
            batchTranslate(from.code, to.code, texts)
        } catch (e: Exception) {
            if (SHOULD_PRINT_ERRORS)
                e.printStackTrace()

            null
        }
    }

    open suspend fun translate(text: String, from: Language, to: Language): String? {
        if (!supportsLanguage(from, to))
            return null

        return try {
            translate(from.code, to.code, text)
        } catch (e: Exception) {
            if (SHOULD_PRINT_ERRORS)
                e.printStackTrace()

            null
        }
    }

    protected fun markTranslating() {
        internalTranslationCount.incrementAndGet()
    }

    protected fun unmarkTranslating() {
        internalTranslationCount.decrementAndGet()
    }

    abstract suspend fun batchTranslate(from: String, to: String, texts: List<String>): List<String>
    abstract suspend fun translate(from: String, to: String, text: String): String

    companion object {
        val SHOULD_PRINT_ERRORS = Flags.PRINT_HTTP_ERRORS
    }
}