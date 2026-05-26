package xyz.bluspring.unitytranslate.api.v2.transcriber

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

abstract class SpeechTranscriber : AutoCloseable {
    /**
     * Initial setup that must occur before this transcriber may be used.
     */
    open val initialSetup: Deferred<Unit> = CompletableDeferred(Unit)

    open fun onSelected() {}
    abstract fun transcribeSamples(samples: FloatArray, langCode: String): Deferred<String>
    abstract override fun close()
}
