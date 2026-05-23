package xyz.bluspring.unitytranslate

import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import java.util.*

class UnityTranslateApiImpl : UnityTranslateApi {
    val transcribers: MutableMap<String, MapCodec<out SpeechTranscriber>> = mutableMapOf()
    val transcriptHolders: MutableMap<String, TranscriptHolder> = WeakHashMap()

    override fun <T : SpeechTranscriber> registerTranscriber(id: String, transcriber: MapCodec<T>) {
        if (this.transcribers.contains(id)) {
            throw IllegalArgumentException("A transcriber already exists by ID $id!")
        }

        if (this.transcribers.containsValue(transcriber)) {
            throw IllegalArgumentException("Tried to register duplicate transcriber codec under two different IDs! (new: $id, existing: ${this.transcribers.filterValues { it == transcriber }.keys.first()})")
        }

        this.transcribers[id] = transcriber
    }

    override fun getTranscriberCodecById(id: String): MapCodec<out SpeechTranscriber>? {
        return this.transcribers[id]
    }

    override fun getTranscriberId(transcriber: SpeechTranscriber): String {
        return this.transcribers.filterValues { it == transcriber.codec }.keys.first()
    }

    override fun getOrCreateTranscriptHolder(languageCode: String): TranscriptHolder {
        return this.transcriptHolders.computeIfAbsent(languageCode, ::TranscriptHolder)
    }

    override fun hasPlugin(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getPluginMetadata(id: String): PluginMetadata? {
        TODO("Not yet implemented")
    }
}
