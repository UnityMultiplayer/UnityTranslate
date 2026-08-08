@file:OptIn(ExperimentalAtomicApi::class)

package xyz.bluspring.unitytranslate.transcriber.google.internal

import com.google.gson.JsonParser
import io.nayuki.flac.common.StreamInfo
import io.nayuki.flac.encode.BitOutputStream
import io.nayuki.flac.encode.FlacEncoder
import io.nayuki.flac.encode.SubframeEncoder
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.util.AudioConverters
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextULong
import kotlin.time.Duration.Companion.seconds

/**
 * This is the internal Google Speech API, utilized by Google Chrome and its forks that don't remove all Google services.
 * The implementation of this is referenced directly from network_speech_recognition_engine_impl.cc in Chromium.
 * However, these *are* internal APIs and keys that can very well go away at any time, so proceed with caution.
 * We do however provide the official paid Google Cloud API transcriber too.
 */
object GoogleInternalTranscriber : SpeechTranscriber() {
    private val logger: Logger = LoggerFactory.getLogger(GoogleInternalTranscriber::class.java)

    // https://giulianopz.github.io/full-duplex-http-streaming-in-go
    // https://gist.github.com/offlinehacker/5780124
    // https://blog.travispayton.com/wp-content/uploads/2014/03/Google-Speech-API.pdf

    // https://github.com/StainlessStlRat/FullDuplexNettyExample

    private const val LOW_BITS = 0x00000000_FFFFFFFFuL
    private const val HIGH_BITS = 0xFFFFFFFF_00000000uL

    const val FULL_DUPLEX_UP_URL = "https://www.google.com/speech-api/full-duplex/v1/up"
    const val FULL_DUPLEX_DOWN_URL = "https://www.google.com/speech-api/full-duplex/v1/down"

    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"

    const val SAMPLE_RATE = AudioHelper.SAMPLE_RATE
    const val FRAME_SIZE = AudioHelper.BUFFER_SIZE
    const val BYTES_PER_SAMPLE = AudioHelper.SAMPLE_SIZE
    const val CHANNELS = AudioHelper.CHANNELS

    private val context = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(context)

    private var streamPair: StreamPair? = null

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

    private data class StreamPair(
        val language: Language,
        // fun fact, Java's stream names are backwards!
        val upStream: Deferred<OutputStream>, // <-- this is the write stream
        val downStream: Deferred<InputStream>, // <-- this is the read stream
        val requestKey: String,

        val upStreamThread: Thread,
        val downStreamThread: Thread,

        var isAlive: AtomicBoolean,

        val readQueue: AtomicReference<String?>,
        val writeQueue: Queue<ByteArray>,
    ) {
        fun transcribe(data: ByteArray): String {
            val startTime = System.currentTimeMillis()
            this.writeQueue.add(data)
            do {
                val text = this.readQueue.load()
                if (text != null)
                    return text
            } while (System.currentTimeMillis() - startTime >= 3.5.seconds.inWholeMilliseconds)

            return ""
        }

        suspend fun await(): StreamPair {
            this.upStream.await()
            this.downStream.await()
            return this
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun createStreams(language: Language): StreamPair {
        val key = this.generateRequestKey()
        val upStream = CompletableDeferred<OutputStream>()
        val downStream = CompletableDeferred<InputStream>()
        val isAlive = AtomicBoolean(true)

        val readQueue = AtomicReference<String?>(null)
        val writeQueue = ConcurrentLinkedQueue<ByteArray>()

        val upStreamThread = thread(isDaemon = true, name = "UnityTranslate Google Internal Transcriber - Upstream Thread") {
            val url = URI("$FULL_DUPLEX_UP_URL?key=${GoogleApiKeys.GOOGLE_API_KEY}&pair=$key&output=json&lang=${language.asBCP47}&pFilter=0&app=chromium&interim&continuous").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "audio/x-flac; rate=16000")
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setChunkedStreamingMode(FRAME_SIZE * 2)
            connection.connect()

            val outputStream = connection.getOutputStream()
            upStream.complete(outputStream)

            while (isAlive.load()) {
                val data = writeQueue.poll()
                if (data != null) {
                    outputStream.write(data)
                }
            }
        }

        val downStreamThread = thread(isDaemon = true, name = "UnityTranslate Google Internal Transcriber - Downstream Thread") {
            val url = URI("$FULL_DUPLEX_DOWN_URL?key=${GoogleApiKeys.GOOGLE_API_KEY}&pair=$key&output=json").toURL()
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.doInput = true
            connection.connect()

            val inputStream = connection.getInputStream()
            downStream.complete(inputStream)

            val reader = inputStream.reader()
            try {
                if (connection.responseCode == 200) {
                    val currentByteList = mutableListOf<Int>()
                    do {
                        val b = inputStream.read()

                        if (b == -1) {
                            isAlive.store(false)
                            break
                        }

                        if (b.toChar() == '\n') {
                            val intArray = currentByteList.toTypedArray().toIntArray()
                            readQueue.store(String(intArray, 0, intArray.size))
                            currentByteList.clear()
                            continue
                        }

                        currentByteList.add(b)
                    } while (isAlive.load())
                }
            } catch (e: Throwable) {
                logger.error("An error occurred during transcription!", e)
            } finally {
                reader.close()
                inputStream.close()
            }

            println("downstream closed")

            val stream = connection.errorStream ?: return@thread
            for (line in stream.reader().readLines()) {
                logger.error("An error occurred in the internal transcriber's downstream: $line")
            }

            stream.close()
        }

        return StreamPair(
            language,
            upStream, downStream,
            key,
            upStreamThread, downStreamThread,
            isAlive,

            readQueue, writeQueue,
        )
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun getOrCreateStreamPair(language: Language): StreamPair {
        val existing = this.streamPair
        if (existing != null && existing.language == language && existing.upStreamThread.isAlive && existing.downStreamThread.isAlive)
            return existing

        if (existing != null) {
            existing.isAlive.store(false)

            withContext(Dispatchers.IO) {
                existing.upStream.await().close()
                existing.downStream.await().close()

                existing.upStreamThread.interrupt()
                existing.downStreamThread.interrupt()
            }
        }

        return this.createStreams(language).await().apply {
            this@GoogleInternalTranscriber.streamPair = this
        }
    }

    override fun transcribeSamples(samples: FloatArray, language: Language): Deferred<String> {
        return this.scope.async {
            val streamPair = getOrCreateStreamPair(language)

            val byteOutputStream = ByteArrayOutputStream()
            val bitStream = BitOutputStream(byteOutputStream)
            bitStream.writeInt(32, 0x664C6143)

            val intSamples = AudioConverters.floatPcm16ToInt(samples)

            val info = StreamInfo().apply {
                sampleRate = SAMPLE_RATE
                numChannels = CHANNELS
                sampleDepth = BYTES_PER_SAMPLE
                numSamples = samples.size.toLong()
            }
            info.write(true, bitStream)
            FlacEncoder(info, arrayOf(intSamples), 4096, SubframeEncoder.SearchOptions.SUBSET_BEST, bitStream)

            bitStream.flush()
            info.write(true, bitStream)
            bitStream.flush()

            val data = streamPair.transcribe(byteOutputStream.toByteArray())
            if (data.isNotBlank()) {
                val json = JsonParser.parseString(data).asJsonObject
                val results = json.getAsJsonArray("result")

                if (!results.isEmpty) {
                    val alts = results[0].asJsonObject.getAsJsonArray("alternative")
                    if (!alts.isEmpty) {
                        return@async alts[0].asJsonObject.get("transcript").asString
                    }
                }
            }

            ""
        }
    }

    override val supportsExternal: Boolean = false
    override val requiresUniqueSamples: Boolean = true

    override fun close() {
        val current = this.streamPair
        if (current != null) {
            runBlocking {
                current.isAlive.store(false)
                current.downStream.await().close()
                current.upStream.await().close()
                current.upStreamThread.interrupt()
                current.downStreamThread.interrupt()
            }
        }
    }
}
