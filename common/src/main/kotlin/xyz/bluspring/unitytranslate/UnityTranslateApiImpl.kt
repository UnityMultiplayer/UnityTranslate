package xyz.bluspring.unitytranslate

import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.InactiveTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.config.SunsetWrappedConfigBuilder
import java.nio.file.Path
import java.util.*

object UnityTranslateApiImpl : UnityTranslateApi {
    val transcribers: MutableMap<String, SpeechTranscriber> = mutableMapOf()
    val transcriberConfigs: MutableMap<String, SunsetConfig> = mutableMapOf()
    val transcriptHolders: MutableMap<String, TranscriptHolder> = WeakHashMap()

    override var activeTranscriber: SpeechTranscriber = InactiveTranscriber

    override val configPath: Path
        get() = PlatformProxy.instance.rootDir.resolve("config/unitytranslate")

    override val storagePath: Path
        get() = PlatformProxy.instance.rootDir.resolve("unitytranslate")

    override fun <T : SpeechTranscriber> registerTranscriber(id: String, value: T, configBuilder: ConfigBuilder.() -> Unit) {
        if (this.transcribers.contains(id)) {
            throw IllegalArgumentException("A transcriber already exists by ID $id!")
        }

        if (this.transcribers.containsValue(value)) {
            throw IllegalArgumentException("Tried to register duplicate transcriber under two different IDs! (new: $id, existing: ${this.transcribers.filterValues { it == value }.keys.first()})")
        }

        this.transcribers[id] = value
        this.transcriberConfigs[id] = SunsetConfig.create(UnityTranslateApi.instance.configPath.resolve("transcribers/$id.json")) {
            val builder = SunsetWrappedConfigBuilder(this)
            configBuilder.invoke(builder)
        }
    }

    override fun getTranscriberId(transcriber: SpeechTranscriber): String {
        return this.transcribers.filterValues { it == transcriber }.keys.first()
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

    override fun registerConfig(id: String, builder: ConfigBuilder.() -> Unit) {
        SunsetConfig.create(UnityTranslateApi.instance.configPath.resolve("plugins/$id.json")) {
            val configBuilder = SunsetWrappedConfigBuilder(this)
            builder.invoke(configBuilder)
        }
    }
}
