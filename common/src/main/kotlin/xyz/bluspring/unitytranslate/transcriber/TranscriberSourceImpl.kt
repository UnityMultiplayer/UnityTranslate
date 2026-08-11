package xyz.bluspring.unitytranslate.transcriber

import com.google.common.collect.Queues
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser
import xyz.bluspring.unitytranslate.api.v2.util.FloatRingBuffer
import kotlin.properties.ReadWriteProperty

class TranscriberSourceImpl(
    override val sender: TranscriptUser,
    transcriber: ReadWriteProperty<Any?, SpeechTranscriber>,
    language: ReadWriteProperty<Any?, Language>
) : TranscriberSource {
    private val speechSamples = FloatRingBuffer(8 * 1024 * 1024) // 8 MiB
    private var isProcessing = false

    val queuedOverflow = Queues.newConcurrentLinkedQueue<FloatArray>()

    var sessionTimestamp = -1L
        private set

    var lastUpdateTimestamp = -1L
        private set

    var transcriber by transcriber
    var language by language

    override fun submitSpeechSamples(samples: FloatArray) {
        synchronized(this.speechSamples) {
            // Guard against accidental overflow
            if (this.speechSamples.totalWritten + samples.size >= this.speechSamples.capacity) {
                synchronized(this.queuedOverflow) {
                    this.queuedOverflow.add(this.speechSamples.snapshot())
                    this.speechSamples.reset()
                }
            }

            this.speechSamples += samples
        }

        this.lastUpdateTimestamp = System.currentTimeMillis()
    }

    val isReadyToProcess: Boolean
        get() = !this.isProcessing && this.speechSamples.updatedSinceMark

    suspend fun processSamples(): Collection<String> {
        this.isProcessing = true
        if (this.sessionTimestamp == -1L)
            this.sessionTimestamp = System.currentTimeMillis()

        val collected = mutableListOf<String>()
        val transcriber = this.transcriber
        val language = transcriber.getEffectiveLanguage(this.language)

        try {
            if (language == null) {
                UnityTranslate.logger.warn("Language ${this.language} is unsupported under transcriber $transcriber!")
                return emptyList()
            }

            while (this.queuedOverflow.isNotEmpty()) {
                val overflow = this.queuedOverflow.poll() ?: break
                collected += transcriber.transcribeSamples(overflow, language).await()
            }

            val samples = synchronized(this.speechSamples) { // make sure we're not concurrently accessing stuff
                this.speechSamples.mark() // mark it so we know when we have updated
                this.speechSamples.snapshot()
            }

            if (transcriber.requiresUniqueSamples)
                this.speechSamples.reset()

            collected += transcriber.transcribeSamples(samples, language).await()

            return collected
        } catch (e: Throwable) {
            UnityTranslate.logger.error("Failed to transcribe text!", e)
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
