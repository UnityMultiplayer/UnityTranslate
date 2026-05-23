package xyz.bluspring.unitytranslate.api.v2

import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import java.util.*

/**
 * THe API surface that allows interacting with UnityTranslate's code directly.
 */
interface UnityTranslateApi {
    /**
     * Registers a speech transcriber into UnityTranslate. The [id] must be unique, and it is recommended to at least prefix the ID with your plugin's ID.
     */
    fun <T : SpeechTranscriber> registerTranscriber(id: String, transcriber: MapCodec<T>)

    /**
     * Attempts to get the transcriber [MapCodec] by the provided ID, or null if none can be found.
     */
    fun getTranscriberCodecById(id: String): MapCodec<out SpeechTranscriber>?

    /**
     * Gets the ID of the provided [SpeechTranscriber].
     */
    fun getTranscriberId(transcriber: SpeechTranscriber): String

    /**
     * Retrieves a [TranscriptHolder] if one is available under the given language code,
     * otherwise creates a new holder.
     *
     * If you want to keep reusing this holder, make sure you always hold a reference to it,
     * as the holder is stored in a [WeakHashMap].
     */
    fun getOrCreateTranscriptHolder(languageCode: String): TranscriptHolder

    fun hasPlugin(group: String, id: String): Boolean = hasPlugin("$group.$id")
    fun hasPlugin(id: String): Boolean

    fun getPluginMetadata(group: String, id: String): PluginMetadata? = getPluginMetadata("$group.$id")
    fun getPluginMetadata(id: String): PluginMetadata?

    companion object {
        @JvmStatic
        val instance: UnityTranslateApi = ServiceLoader.load(UnityTranslateApi::class.java).first()
    }
}
