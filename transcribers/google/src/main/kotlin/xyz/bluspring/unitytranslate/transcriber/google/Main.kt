package xyz.bluspring.unitytranslate.transcriber.google

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.sourceforge.javaflacencoder.FLACEncoder
import net.sourceforge.javaflacencoder.FLACStreamOutputStream
import net.sourceforge.javaflacencoder.StreamConfiguration
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextULong

object Main {
    private const val LOW_BITS = 0x00000000_FFFFFFFFuL
    private const val HIGH_BITS = 0xFFFFFFFF_00000000uL

    const val USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"

    const val SAMPLE_RATE = 16_000
    const val FRAME_SIZE = (SAMPLE_RATE / 1000) * 20

    private fun generateRequestKey(): String {
        val time = System.currentTimeMillis().toULong()
        val timeLow = time and LOW_BITS

        val random = Random.nextULong()
        val randomHigh = random and HIGH_BITS

        return (timeLow or randomHigh).toHexString()
    }

    // https://giulianopz.github.io/full-duplex-http-streaming-in-go
    // https://gist.github.com/offlinehacker/5780124
    // https://blog.travispayton.com/wp-content/uploads/2014/03/Google-Speech-API.pdf

    // https://github.com/StainlessStlRat/FullDuplexNettyExample

    /*

     */

    @JvmStatic
    fun main(args: Array<out String>) {
        val requestKey = generateRequestKey()
        // network_speech_recognition_engine_impl.cc

        runBlocking {
            val byteStream = QueuedByteArrayOutputStream(FRAME_SIZE)
            val outputStream = BufferedOutputStream(byteStream)
            val encoder = FLACEncoder()

            encoder.threadCount = 1
            encoder.setStreamConfiguration(StreamConfiguration(1, 16, FRAME_SIZE, SAMPLE_RATE, 16))

            val audioFormat = AudioFormat(SAMPLE_RATE.toFloat(), 16, 1, true, false)
            val mic = AudioSystem.getTargetDataLine(audioFormat)
            mic.open(audioFormat)
            val audioStream = AudioInputStream(mic)
            mic.start()

            println("Loaded mic")

            // Mic thread
            launch {
                while (mic.isOpen) {
                    val bytesRead = ByteArray(FRAME_SIZE)
                    audioStream.read(bytesRead)
                    outputStream.write(bytesRead)
                }

                /*while (mic.isOpen) {
                    if (mic.available() < FRAME_SIZE) {
                        Thread.sleep(5)
                        continue
                    }

                    val byteArray = ByteArray(FRAME_SIZE * 2)
                    mic.read(byteArray, 0, byteArray.size)

                    val intBuffer = ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()
                    val intArray = IntArray(intBuffer.remaining())
                    intBuffer.get(intArray)

                    encoder.addSamples(intArray, 1)
                }*/
            }
                .start()

            // Upstream thread - sends the data directly to the API.
            thread {
                println("Started upstream")
                val url = URI.create("https://www.google.com/speech-api/full-duplex/v1/up?key=${GoogleApiKeys.GOOGLE_API_KEY}&pair=${requestKey}&output=json&lang=en-US&pFilter=0&app=chromium&continuous&interim").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30_000
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "audio/x-flac; rate=16000")
                connection.setRequestProperty("User-Agent", USER_AGENT)
                connection.setChunkedStreamingMode(FRAME_SIZE * 2)
                connection.connect()

                val netStream = connection.getOutputStream()
                //outputStream = connection.getOutputStream()//BufferedOutputStream(, FRAME_SIZE * 2)
                encoder.clear()
                encoder.setOutputStream(FLACStreamOutputStream(netStream))
                encoder.openFLACStream()
                println("Loaded upstream")

                byteStream.whatToWrite = { array ->
                    val stream = netStream

                    encoder.addSamples(array, array.size / 2)
                    encoder.encodeSamples(array.size / 2, false)
                }
            }

            // Downstream thread - receives the data from the API.
            thread {
                println("Started downstream")
                val url = URI.create("https://www.google.com/speech-api/full-duplex/v1/down?key=${GoogleApiKeys.GOOGLE_API_KEY}&pair=${requestKey}&output=json").toURL()
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30_000
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", USER_AGENT)
                connection.doInput = true
                connection.connect()

                try {
                    val reader = connection.getInputStream().reader()
                    println("Loaded downstream")
                    while (connection.responseCode == 200) {
//                        println(reader.read())
                        if (reader.ready()) {
                            val line = reader.readText()
                            println(line)
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                val stream = connection.errorStream ?: return@thread
                for (line in stream.reader().readLines()) {
                    println("Error downstream: $line")
                }
            }
        }
        // this is where the data comes from
        //val downUrl = HttpUtil.post("https://www.google.com/speech-api/full-duplex/v1/down?key=${GoogleApiKeys.GOOGLE_API_KEY}&pair=${requestKey}&output=pb", JsonObject())

        // this needs an octet-stream of the wav
        //val upUrl = HttpUtil.post("https://www.google.com/speech-api/full-duplex/v1/up?key=${GoogleApiKeys.GOOGLE_API_KEY}&pair=${requestKey}&output=pb&lang=en-US&pFilter=0&app=chromium&continuous&audioFormat=audio/wav", JsonObject())
    }
}