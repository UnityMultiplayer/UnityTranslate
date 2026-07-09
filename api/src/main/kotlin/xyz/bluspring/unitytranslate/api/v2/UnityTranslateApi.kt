package xyz.bluspring.unitytranslate.api.v2

import com.mojang.serialization.MapCodec
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.display.LanguageDisplay
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.InactiveTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriberSource
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorManager
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
     * Registers a translator into UnityTranslate. The [id] must be unique, and it is recommended to at least prefix the ID with your plugin's ID.
     */
    fun <T : TranslatorInstance> registerTranslator(id: String, value: T, configBuilder: ConfigBuilder.() -> Unit)

    /**
     * Gets the translator manager.
     */
    val translatorManager: TranslatorManager

    /**
     * Registers a speech transcriber into UnityTranslate. The [id] must be unique, and it is recommended to at least prefix the ID with your plugin's ID.
     */
    fun <T : SpeechTranscriber> registerTranscriber(id: String, value: T, configBuilder: ConfigBuilder.() -> Unit)

    /**
     * Registers a transcriber source into UnityTranslate. The [sender] should refer to whoever is sending the transcript.
     * This transcriber may be discarded at any time by simply dereferencing it.
     */
    fun getOrCreateTranscriberSource(sender: TranscriptUser): TranscriberSource

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
    fun getOrCreateTranscriptHolder(language: Language): TranscriptHolder

    fun hasPlugin(group: String, id: String): Boolean = hasPlugin("$group.$id")
    fun hasPlugin(id: String): Boolean

    fun getPluginMetadata(group: String, id: String): PluginMetadata? = getPluginMetadata("$group.$id")
    fun getPluginMetadata(id: String): PluginMetadata?

    fun registerConfig(group: String, id: String, builder: ConfigBuilder.() -> Unit) = registerConfig("$group.$id", builder)
    fun registerConfig(id: String, builder: ConfigBuilder.() -> Unit)

    fun registerOutputLanguage(group: String, id: String): LanguageHolder = registerOutputLanguage("$group.$id")
    fun registerOutputLanguage(id: String): LanguageHolder

    fun <T : LanguageDisplay> registerLanguageDisplay(group: String, id: String, codec: MapCodec<T>) = registerLanguageDisplay("$group/$id", codec)
    fun <T : LanguageDisplay> registerLanguageDisplay(id: String, codec: MapCodec<T>)

    fun getLanguageDisplay(id: String): MapCodec<out LanguageDisplay>?
    fun getLanguageDisplayId(codec: MapCodec<out LanguageDisplay>): String
    fun getLanguageDisplayId(display: LanguageDisplay): String {
        return this.getLanguageDisplayId(display.codec)
    }

    /**
     * Gets the current spoken language. This is typically used as the default output language if none is set.
     */
    val currentSpokenLanguage: Language

    /**
     * Gets the default font used in this instance of UnityTranslate.
     */
    val defaultFont: FontReference

    companion object {
        @JvmStatic
        val instance: UnityTranslateApi = Class.forName("xyz.bluspring.unitytranslate.UnityTranslateApiImpl").getField("INSTANCE").get(null) as UnityTranslateApi
    }
}
