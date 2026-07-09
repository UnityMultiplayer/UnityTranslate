package xyz.bluspring.unitytranslate.client.transcriber.source

import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import kotlin.concurrent.thread

abstract class InputSource(val source: TranscriberSource) {
    protected val thread: Thread
    private var isRunning = true

    var isMuted = false

    init {
        this.thread = thread(start = true, isDaemon = true, name = "UnityTranslate Input Source for ${this.source}") {
            while (this.isRunning) {
                if (this.isMuted) continue

                val available = this.available
                if (available >= AudioHelper.BUFFER_SIZE) {
                    val samples = this.sample()
                    this.source.submitSpeechSamples(samples)
                }
            }
        }
    }

    abstract val available: Int
    abstract fun sample(): FloatArray

    open fun close() {
        this.isRunning = false
    }
}
