package xyz.bluspring.unitytranslate.transcriber.whisper

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import java.io.OutputStream

class WhisperTranscriber : SpeechTranscriber() {
    override fun handleStream(output: OutputStream) {
        TODO("Not yet implemented")
    }

    companion object {
        @JvmField val CODEC: MapCodec<WhisperTranscriber> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(

            )
                .apply(instance, ::WhisperTranscriber)
        }
    }
}
