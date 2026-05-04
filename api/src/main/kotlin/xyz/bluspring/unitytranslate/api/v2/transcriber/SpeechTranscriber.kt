package xyz.bluspring.unitytranslate.api.v2.transcriber

import java.io.OutputStream

interface SpeechTranscriber {
    fun handleStream(output: OutputStream)
}