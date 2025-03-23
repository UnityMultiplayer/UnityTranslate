package xyz.bluspring.unitytranslate.transcriber.whisper

import io.github.givimad.whisperjni.WhisperContext
import io.github.givimad.whisperjni.WhisperContextParams
import io.github.givimad.whisperjni.WhisperJNI
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.common.util.nativeaccess.CudaHelper
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists

// Realtime Whisper transcription, based on https://github.com/ggerganov/whisper.cpp/blob/master/examples/stream/stream.cpp
class WhisperTranscriber(instance: UnityTranslate, language: Language) : SpeechTranscriber(TranscriberType.WHISPER, instance, language) {
    val whisper = WhisperJNI()
    val ctx: WhisperContext

    private val states = ConcurrentHashMap<UUID, WhisperTranscriptState>()

    init {
        if (!isAvailable())
            throw IllegalStateException("How was this initialized?")

        if (!language.supportedTranscribers.contains(TranscriberType.WHISPER))
            throw IllegalStateException("Langugage $language does not support Whisper!")

        ctx = whisper.init(whisperPath.resolve(UnityTranslateMCClient.clientConfig.whisperModel.fileName),
            WhisperContextParams().apply {
                useGPU = CudaHelper.isCudaSupported
            })
    }

    fun createState(uuid: UUID, updater: (String) -> Unit): WhisperTranscriptState {
        return states.computeIfAbsent(uuid) { WhisperTranscriptState(it, this, updater) }
    }

    fun transcribe(uuid: UUID, samples: FloatArray) {
        if (!states.contains(uuid))
            return

        states[uuid]!!.update(samples)
    }

    override fun stop() {
        ctx.close()
        for ((_, state) in states) {
            state.close()
        }
        states.clear()
    }

    companion object {
        const val WHISPER_SAMPLE_RATE = 16_000
        const val LENGTH_MS = 10_000
        const val KEEP_MS = 200
        const val MAX_TOKENS = 32
        const val FREQUENCY_THRESHOLD = 100f

        const val SAMPLES_LENGTH = ((1e-3 * LENGTH_MS) * WHISPER_SAMPLE_RATE).toInt()
        const val SAMPLES_KEEP = ((1e-3 * KEEP_MS) * WHISPER_SAMPLE_RATE).toInt()
        const val SAMPLES_30S = ((1e-3 * 30_000) * WHISPER_SAMPLE_RATE).toInt()

        private val logger = LoggerFactory.getLogger("UnityTranslate/Whisper")
        val whisperPath = UnityTranslate.instance.path.resolve("whisper_models")

        fun isAvailable(): Boolean {
            if (!whisperPath.exists())
                return false

            if (!whisperPath.resolve(UnityTranslateMCClient.clientConfig.whisperModel.fileName).exists())
                return false

            return true
        }

        fun tryInitialize() {
            if (isAvailable()) {
                TranscriberType.WHISPER.creator = ::WhisperTranscriber
            }
        }

        init {
            WhisperJNI.loadLibrary()
            WhisperJNI.setLibraryLogger {
                logger.debug(it)
            }
        }
    }
}