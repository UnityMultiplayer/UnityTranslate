package xyz.bluspring.unitytranslate.api.v2.transcriber

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import java.io.OutputStream

abstract class SpeechTranscriber {
    abstract val codec: MapCodec<out SpeechTranscriber>

    abstract fun handleStream(output: OutputStream)

    companion object {
        @JvmField
        val CODEC: Codec<SpeechTranscriber> = Codec.STRING.dispatch("type", { transcriber -> UnityTranslateApi.instance.getTranscriberId(transcriber) }) { id ->
            UnityTranslateApi.instance.getTranscriberCodecById(id)!!
        }
    }
}
