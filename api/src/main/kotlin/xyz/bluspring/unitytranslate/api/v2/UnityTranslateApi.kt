package xyz.bluspring.unitytranslate.api.v2

import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import java.util.*

/**
 * THe API surface that allows interacting with UnityTranslate's code directly.
 */
interface UnityTranslateApi {
    /**
     * Registers a speech transcriber into UnityTranslate. The [id] must be unique, and it is recommended to at least prefix the ID with your plugin's ID.
     */
    fun registerTranscriber(id: String, transcriber: SpeechTranscriber)

    fun hasPlugin(group: String, id: String): Boolean = hasPlugin("$group.$id")
    fun hasPlugin(id: String): Boolean

    fun getPluginMetadata(group: String, id: String): PluginMetadata? = getPluginMetadata("$group.$id")
    fun getPluginMetadata(id: String): PluginMetadata?

    companion object {
        @JvmStatic
        val instance: UnityTranslateApi = ServiceLoader.load(UnityTranslateApi::class.java).first()
    }
}
