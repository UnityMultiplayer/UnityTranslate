package xyz.bluspring.unitytranslate.api.v2.transcriber.processor

import xyz.bluspring.unitytranslate.api.v2.Language

interface TranscriptPreProcessor {
    fun processTranscript(text: String, originalText: String, language: Language): String
}
