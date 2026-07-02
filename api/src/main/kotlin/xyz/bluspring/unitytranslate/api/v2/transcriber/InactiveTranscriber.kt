package xyz.bluspring.unitytranslate.api.v2.transcriber

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import xyz.bluspring.unitytranslate.api.v2.Language

/**
 * Represents an inactive transcriber that will simply not transcribe any data.
 * Used as a fallback when a transcriber fails to load.
 */
object InactiveTranscriber : SpeechTranscriber() {
    override suspend fun supportsLanguage(language: Language): Boolean = true
    override fun transcribeSamples(samples: FloatArray, language: Language): Deferred<String> = CompletableDeferred("")

    override fun close() {
    }
}
