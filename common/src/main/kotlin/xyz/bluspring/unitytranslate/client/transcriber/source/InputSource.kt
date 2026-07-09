package xyz.bluspring.unitytranslate.client.transcriber.source

import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource

abstract class InputSource(val source: TranscriberSource) {
    init {

    }

    abstract val available: Int
    abstract fun sample(): FloatArray
    abstract fun close()
}
