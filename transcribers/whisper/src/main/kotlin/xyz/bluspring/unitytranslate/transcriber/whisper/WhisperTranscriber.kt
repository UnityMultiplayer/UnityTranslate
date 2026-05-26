package xyz.bluspring.unitytranslate.transcriber.whisper

import dev.cadindie.whisper4j.Whisper
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import java.io.IOException
import java.nio.file.Files
import java.util.*
import kotlin.io.path.createParentDirectories
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
    private val logger: Logger = LoggerFactory.getLogger(WhisperTranscriber::class.java)

    private fun createContextThreads() = Dispatchers.Default.limitedParallelism(maxWhisperThreads) + CoroutineName("UnityTranslate Whisper Transcriber")

    private val whisperInstances = Collections.synchronizedMap(mutableMapOf<String, Whisper>())

    override val initialSetup: Deferred<Unit>
        get() = CoroutineScope(Dispatchers.IO).async {
            model.path.createParentDirectories()
            val storage = Files.getFileStore(model.path.parent)
            if (storage.usableSpace < model.minimumBytes) {
                logger.error("Not enough storage to download ${model.fileName}! (expected: ${model.minimumBytes}, actual: ${storage.usableSpace})")
                throw IOException("Not enough storage to download ${model.fileName}! (expected: ${model.minimumBytes}, actual: ${storage.usableSpace})")
            }

            val download = DownloadHelper.queue(model)
            download.onStartDownload.register {
                logger.info("Downloading ${model.fileName}...")
            }

            download.onFinishDownload.register {
                if (download.deferred.isCancelled || download.deferred.getCompletionExceptionOrNull() != null) {
                    logger.error("Failed to download ${model.fileName}!")
                } else {
                    logger.info("Successfully downloaded ${model.fileName}!")
                }
            }

            download.deferred.await()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onSelected() {
        super.onSelected()
    }

    override fun transcribeSamples(samples: FloatArray, langCode: String): Deferred<String> {
        return this.scope.async(this.context) {
            // Wait for the Whisper model to be downloaded first.
            if (!model.path.exists()) {
                return@async ""
            }

            // Initialize Whisper instance for this specific language.
            val whisper = synchronized(whisperInstances) {
                whisperInstances.computeIfAbsent(langCode) {
                    createWhisperInstance(langCode)
                }
            }

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
