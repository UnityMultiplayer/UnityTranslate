package xyz.bluspring.unitytranslate.transcriber.whisper

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import java.io.OutputStream

class WhisperTranscriber(
    val model: WhisperModel,
) : SpeechTranscriber() {
    override fun handleStream(output: OutputStream) {
        TODO("Not yet implemented")
    }

    override val codec: MapCodec<out SpeechTranscriber>
        get() = CODEC

    companion object {
        @JvmField val CODEC: MapCodec<WhisperTranscriber> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                WhisperModel.CODEC.fieldOf("model")
                    .forGetter(WhisperTranscriber::model),
            )
                .apply(instance, ::WhisperTranscriber)
        }
    }
}
