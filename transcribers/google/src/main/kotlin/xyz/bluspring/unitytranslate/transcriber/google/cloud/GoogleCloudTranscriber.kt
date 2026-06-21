package xyz.bluspring.unitytranslate.transcriber.google.cloud

import kotlinx.coroutines.Deferred
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber

object GoogleCloudTranscriber : SpeechTranscriber() {
    var apiKey: String = ""

    override suspend fun supportsLanguage(langCode: String): Boolean {
        if (this.apiKey.isBlank())
            return false

        return false
    }

    override fun transcribeSamples(
        samples: FloatArray,
        langCode: String
    ): Deferred<String> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}
