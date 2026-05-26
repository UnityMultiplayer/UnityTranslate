package xyz.bluspring.unitytranslate.api.v2

import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.InactiveTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import java.nio.file.Path
import java.util.*

/**
 * THe API surface that allows interacting with UnityTranslate's code directly.
 */
interface UnityTranslateApi {
    /**
     * The main UnityTranslate storage path. In modded environments, this is under the "unitytranslate" directory.
     * May not exist, you may need to create it yourself.
     */
    val storagePath: Path

    /**
     * The UnityTranslate config path. In modded environments, this is under the "config/unitytranslate" directory.
     * May not exist, you may need to create it yourself.
     */
    val configPath: Path

    /**
     * Registers a speech transcriber into UnityTranslate. The [id] must be unique, and it is recommended to at least prefix the ID with your plugin's ID.
     */
    fun <T : SpeechTranscriber> registerTranscriber(id: String, value: T, configBuilder: ConfigBuilder.() -> Unit)

    /**
     * Gets the ID of the provided [SpeechTranscriber].
     */
    fun getTranscriberId(transcriber: SpeechTranscriber): String

    /**
     * Gets a [SpeechTranscriber] by ID. Defaults to [InactiveTranscriber] if none exists.
     */
    fun getTranscriber(id: String): SpeechTranscriber

    /**
     * Gets the currently active [SpeechTranscriber].
     * If no transcriber is active, it will default to [InactiveTranscriber]
     */
    val activeTranscriber: SpeechTranscriber

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

    fun registerConfig(group: String, id: String, builder: ConfigBuilder.() -> Unit) = registerConfig("$group.$id", builder)
    fun registerConfig(id: String, builder: ConfigBuilder.() -> Unit)

    companion object {
        @JvmStatic
        val instance: UnityTranslateApi = ServiceLoader.load(UnityTranslateApi::class.java).first()
    }
}
