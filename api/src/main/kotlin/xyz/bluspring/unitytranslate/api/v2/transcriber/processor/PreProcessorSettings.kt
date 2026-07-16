package xyz.bluspring.unitytranslate.api.v2.transcriber.processor

import xyz.bluspring.unitytranslate.api.v2.Language

@JvmRecord
data class PreProcessorSettings @JvmOverloads constructor(
    // If null, means supports all languages.
    val transcriberLanguages: Set<Language>? = null,
)
