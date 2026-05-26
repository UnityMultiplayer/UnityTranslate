package xyz.bluspring.unitytranslate.transcriber.whisper

import dev.cadindie.whisper4j.Whisper
import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import java.util.*
import kotlin.io.path.exists

object WhisperTranscriber : SpeechTranscriber() {
    var model: WhisperModel = WhisperModel.TINY
        set(value) {
            field = value
            this.close() // Invalidate all existing instances
        }
    var maxWhisperThreads: Int = 3
        set(value) {
            field = value
            this.context.runCatching { cancel() }
            this.scope.runCatching { cancel("Threads adjusted") }
            this.context = this.createContextThreads() // Recreate the coroutine contexts
            this.scope = CoroutineScope(this.context)
        }
    var enableGpu: Boolean = false
        set(value) {
            field = value
            this.close() // Invalidate all existing instances
        }

    private var context = createContextThreads()
    private var scope = CoroutineScope(context)

    private fun createContextThreads() = Dispatchers.Default.limitedParallelism(maxWhisperThreads) + CoroutineName("UnityTranslate Whisper Transcriber")

    private val whisperInstances = Collections.synchronizedMap(mutableMapOf<String, Whisper>())

    override fun transcribeSamples(samples: FloatArray, langCode: String): Deferred<String> {
        // Initialize Whisper instance for this specific language.
        val whisper = synchronized(whisperInstances) {
            this.whisperInstances.computeIfAbsent(langCode) {
                createWhisperInstance(langCode)
            }
        }

        return this.scope.async(this.context) {
            whisper.transcribeRaw(samples)
        }
    }

    override fun close() {
        synchronized(this.whisperInstances) {
            for ((_, whisper) in this.whisperInstances) {
                whisper.close()
            }

            this.whisperInstances.clear()
        }
    }

    private fun createWhisperInstance(langCode: String): Whisper {
        if (!this.model.path.exists())
            throw IllegalStateException("Whisper model ${this.model.name} has not been downloaded yet!")

        return Whisper.Builder()
            .setLanguage(langCode)
            .setModel(this.model.path.toFile())
            .setUseGpu(this.enableGpu)
            .setDebugInfo(true)
            .build().apply {
                this.initialize()
            }
    }
}
