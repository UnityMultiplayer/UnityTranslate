package xyz.bluspring.unitytranslate.transcriber

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptSender
import xyz.bluspring.unitytranslate.api.v2.util.FloatRingBuffer
import kotlin.properties.ReadWriteProperty

class TranscriberSourceImpl(
    override val sender: TranscriptSender,
    transcriber: ReadWriteProperty<Any?, SpeechTranscriber>,
    language: ReadWriteProperty<Any?, Language>
) : TranscriberSource {
    private val speechSamples = FloatRingBuffer(1 * 1024 * 1024) // 1 MiB
    private var isProcessing = false

    var sessionTimestamp = -1L
        private set

    var transcriber by transcriber
    var language by language

    override fun submitSpeechSamples(samples: FloatArray) {
        synchronized(this.speechSamples) {
            this.speechSamples += samples
        }
    }

    val isReadyToProcess: Boolean
        get() = !this.isProcessing && this.speechSamples.updatedSinceMark

    suspend fun processSamples(): String {
        this.isProcessing = true
        if (this.sessionTimestamp == -1L)
            this.sessionTimestamp = System.currentTimeMillis()

        try {
            val samples = synchronized(this.speechSamples) { // make sure we're not concurrently accessing stuff
                this.speechSamples.mark() // mark it so we know when we have updated
                this.speechSamples.snapshot()
            }

            return this.transcriber.transcribeSamples(samples, this.language).await()
        } catch (e: Throwable) {
            throw e
        } finally {
            this.isProcessing = false
        }
    }

    override fun reset() {
        this.speechSamples.reset()
        this.sessionTimestamp = -1L
    }
}
