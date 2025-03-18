package xyz.bluspring.unitytranslate.transcriber.whisper

import io.github.givimad.whisperjni.WhisperFullParams
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import java.util.*

class WhisperTranscriptState(val uuid: UUID, val transcriber: WhisperTranscriber, val updater: (String) -> Unit) : AutoCloseable {
    val whisper = transcriber.whisper
    val ctx = transcriber.ctx
    val language = transcriber.language
    val whisperState = whisper.initState(transcriber.ctx)

    var current = FloatArray(WhisperTranscriber.SAMPLES_30S)
    var old = FloatArray(WhisperTranscriber.SAMPLES_30S)

    fun update(samples: FloatArray) {
        val take = old.size.coerceAtMost((WhisperTranscriber.SAMPLES_KEEP + WhisperTranscriber.SAMPLES_LENGTH - samples.size).coerceAtLeast(0))
        current = FloatArray(samples.size + take)

        for (i in 0 until take) {
            current[i] = old[old.size - take + i]
        }

        System.arraycopy(samples, 0, current, take, samples.size)
        old = current

        transcribe()
    }

    fun transcribe() {
        val params = WhisperFullParams()
        params.language = language.supportedTranscribers[TranscriberType.WHISPER]
        params.nThreads = 2
        params.singleSegment = true
        params.detectLanguage = false
        params.noTimestamps = true
        params.translate = false
        params.suppressBlank = true
        params.suppressNonSpeechTokens = true
        params.beamSearchBeamSize = 5
        params.noContext = false
        params.temperatureInc = 0f

        val result = whisper.fullWithState(ctx, whisperState, params, current, current.size)

        if (result != 0) {
            throw RuntimeException("Transcription for language $language failed with code $result.")
        }

        val length = whisper.fullNSegmentsFromState(whisperState)
        val texts = mutableListOf<String>()

        for (i in 0 until length) {
            texts.add(whisper.fullGetSegmentTextFromState(whisperState, i))
        }

        updater.invoke(texts.joinToString(" "))
    }

    override fun close() {
        whisperState.close()
    }
}
