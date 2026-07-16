package xyz.bluspring.unitytranslate.api.v2.transcriber.processor

import xyz.bluspring.unitytranslate.api.v2.Language

@JvmRecord
data class PostProcessorSettings @JvmOverloads constructor(
    // If null, means supports all languages.
    val translatorInputLanguages: Set<Language>? = null,
    val translatorOutputLanguages: Set<Language>? = null,
)
