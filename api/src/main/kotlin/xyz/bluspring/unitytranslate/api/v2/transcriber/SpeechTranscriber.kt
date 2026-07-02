package xyz.bluspring.unitytranslate.api.v2.transcriber

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi

abstract class SpeechTranscriber : AutoCloseable {
    /**
     * Initial setup that must occur before this transcriber may be used.
     */
    open val initialSetup: Deferred<Unit> = CompletableDeferred(Unit)

    /**
     * Allows using this transcriber for transcribing external sources, such as other players who do not have UnityTranslate installed, or certain programs that are outputting audio data
     * that needs to be translated.
     */
    open val supportsExternal: Boolean = true

    abstract suspend fun supportsLanguage(language: Language): Boolean

    open fun onSelected() {}
    abstract fun transcribeSamples(samples: FloatArray, language: Language): Deferred<String>
    abstract override fun close()

    companion object {
        @JvmField val CODEC: Codec<SpeechTranscriber> = Codec.STRING.dispatch("type", {
            UnityTranslateApi.instance.getTranscriberId(it)
        }, {
            MapCodec.unit { UnityTranslateApi.instance.getTranscriber(it) }
        })
    }
}
