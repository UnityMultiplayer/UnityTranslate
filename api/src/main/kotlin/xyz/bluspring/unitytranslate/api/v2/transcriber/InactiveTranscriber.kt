package xyz.bluspring.unitytranslate.api.v2.transcriber

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

/**
 * Represents an inactive transcriber that will simply not transcribe any data.
 * Used as a fallback when a transcriber fails to load.
 */
object InactiveTranscriber : SpeechTranscriber() {
    override fun transcribeSamples(samples: FloatArray, langCode: String): Deferred<String> {
        return CompletableDeferred("")
    }

    override fun close() {
    }
}
