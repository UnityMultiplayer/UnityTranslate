package xyz.bluspring.unitytranslate.api.v2.transcriber.processor

import xyz.bluspring.unitytranslate.api.v2.Language

interface TranscriptPostProcessor {
    fun processFinalTranscript(text: String, originalText: String, sourceLanguage: Language, currentLanguage: Language): String
}
