package xyz.bluspring.unitytranslate.transcriber

import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import xyz.bluspring.unitytranslate.api.v2.transcriber.sampling.TranscriberSamplingStrategy
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser
import kotlin.properties.ReadWriteProperty

class TranscriberSourceImpl(
    override val sender: TranscriptUser,
    transcriber: ReadWriteProperty<Any?, SpeechTranscriber>,
    language: ReadWriteProperty<Any?, Language>
) : TranscriberSource {
    private var isProcessing = false
    private var lastTranscriber: SpeechTranscriber? = null
    private var currentSamplingStrategy: TranscriberSamplingStrategy? = null

    val samplingStrategy: TranscriberSamplingStrategy
        get() {
            if (this.currentSamplingStrategy == null || this.lastTranscriber != this.transcriber) {
                this.currentSamplingStrategy = this.transcriber.samplingStrategyProvider()
                this.lastTranscriber = this.transcriber
            }

            return this.currentSamplingStrategy!!
        }

    var sessionTimestamp = -1L
        private set

    var lastUpdateTimestamp = -1L
        private set

    var transcriber by transcriber
    var language by language

    override fun submitSpeechSamples(samples: FloatArray) {
        this.samplingStrategy.submitSamples(samples)
        this.lastUpdateTimestamp = System.currentTimeMillis()
    }

    val isReadyToProcess: Boolean
        get() = !this.isProcessing && this.samplingStrategy.isUpdated

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

            val samples = this.samplingStrategy.collectSamples()
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
        this.samplingStrategy.reset()
        this.sessionTimestamp = -1L
    }
}
