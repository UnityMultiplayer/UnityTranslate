package xyz.bluspring.unitytranslate.api.v2.transcriber

import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser

/**
 * Each instance of this transcriber should only be for one source!
 */
interface TranscriberSource {
    /**
     * The sender associated with this transcriber source.
     */
    val sender: TranscriptUser

    /**
     * Submits speech samples for transcription. Avoid sending silence, any form of VAD
     * must be handled on your end.
     */
    fun submitSpeechSamples(samples: FloatArray)

    /**
     * Resets the speech sample cache. Call this when you are encountering a period of silence.
     */
    fun reset()
}
