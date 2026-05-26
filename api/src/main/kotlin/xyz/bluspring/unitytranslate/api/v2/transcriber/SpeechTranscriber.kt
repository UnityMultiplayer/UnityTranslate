package xyz.bluspring.unitytranslate.api.v2.transcriber

import kotlinx.coroutines.Deferred

abstract class SpeechTranscriber : AutoCloseable {
    open fun onSelected() {}
    abstract fun transcribeSamples(samples: FloatArray, langCode: String): Deferred<String>
    abstract override fun close()
}
