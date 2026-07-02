package xyz.bluspring.unitytranslate.transcriber.google.internal

import kotlinx.coroutines.Deferred
import net.sourceforge.javaflacencoder.FLACEncoder
import net.sourceforge.javaflacencoder.FLACStreamOutputStream
import net.sourceforge.javaflacencoder.StreamConfiguration
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import kotlin.random.nextULong

/**
 * This is the internal Google Speech API, utilized by Google Chrome and its forks that don't remove all Google services.
 * The implementation of this is referenced directly from network_speech_recognition_engine_impl.cc in Chromium.
 * However, these *are* internal APIs and keys that can very well go away at any time, so proceed with caution.
 * We do however provide the official paid Google Cloud API transcriber too.
 */
object GoogleInternalTranscriber : SpeechTranscriber() {
    // https://giulianopz.github.io/full-duplex-http-streaming-in-go
    // https://gist.github.com/offlinehacker/5780124
    // https://blog.travispayton.com/wp-content/uploads/2014/03/Google-Speech-API.pdf

    // https://github.com/StainlessStlRat/FullDuplexNettyExample

    private var currentLang = ""

    private const val LOW_BITS = 0x00000000_FFFFFFFFuL
    private const val HIGH_BITS = 0xFFFFFFFF_00000000uL

    const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

    const val SAMPLE_RATE = 16_000
    const val FRAME_SIZE = (SAMPLE_RATE / 1000) * 20
    const val BYTES_PER_SAMPLE = 16
    const val CHANNELS = 1

    private val flacEncoder = FLACEncoder()
    private val streamConfig = StreamConfiguration().apply {
        this.sampleRate = SAMPLE_RATE
        this.bitsPerSample = 16
        this.channelCount = 1
    }

    init {
        this.flacEncoder.setStreamConfiguration(this.streamConfig)
    }

    private fun generateRequestKey(): String {
        val time = System.currentTimeMillis().toULong()
        val timeLow = time and LOW_BITS

        val random = Random.nextULong()
        val randomHigh = random and HIGH_BITS

        return (timeLow or randomHigh).toHexString()
    }

    override suspend fun supportsLanguage(language: Language): Boolean {
        return true
    }

    override fun transcribeSamples(samples: FloatArray, language: Language): Deferred<String> {
        // This uses speech recognition v1 for now rather than full duplex, we don't want to lose the
        // connection from silence.

        val byteOutputStream = ByteArrayOutputStream()
        val flacOutput = FLACStreamOutputStream(byteOutputStream)

        // Trying to recreate the audio input stream stuff from scratch...


        synchronized(this.flacEncoder) {
            this.flacEncoder.setOutputStream(flacOutput)
            this.flacEncoder.openFLACStream()

            val maxRead = samples.size * Float.SIZE_BYTES
        }
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}
