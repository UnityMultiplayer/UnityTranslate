package xyz.bluspring.unitytranslate

import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.api.v2.LanguageHolder
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.transcriber.InactiveTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorManager
import xyz.bluspring.unitytranslate.config.builders.SunsetWrappedConfigBuilder
import xyz.bluspring.unitytranslate.translator.TranslatorManagerImpl
import xyz.bluspring.unitytranslate.translator.instance.InactiveTranslatorInstance
import java.nio.file.Path
import java.util.*

object UnityTranslateApiImpl : UnityTranslateApi {
    val transcribers: MutableMap<String, SpeechTranscriber> = mutableMapOf()
    val transcriberConfigs: MutableMap<String, SunsetConfig> = mutableMapOf()
    val transcriptHolders: MutableMap<String, TranscriptHolder> = WeakHashMap()

    val translators: MutableMap<String, TranslatorInstance> = mutableMapOf()
    val translatorConfigs: MutableMap<String, SunsetConfig> = mutableMapOf()

    val outputLanguages: MutableMap<String, LanguageHolder> = mutableMapOf()
    override var currentSpokenLanguage: String = "en"
    override val translatorManager: TranslatorManager = TranslatorManagerImpl

    override var activeTranscriber: SpeechTranscriber = InactiveTranscriber
        private set

    suspend fun setActiveTranscriber(transcriber: SpeechTranscriber) {
        this.activeTranscriber.close()
        transcriber.initialSetup.await()
        this.activeTranscriber = transcriber
    }

    override val configPath: Path
        get() = PlatformProxy.instance.rootDir.resolve("config/unitytranslate")

    override val storagePath: Path
        get() = PlatformProxy.instance.rootDir.resolve("unitytranslate")

    override fun <T : TranslatorInstance> registerTranslator(id: String, value: T, configBuilder: ConfigBuilder.() -> Unit) {
        if (this.translators.contains(id)) {
            throw IllegalArgumentException("A translator already exists by ID $id!")
        }

        if (this.translators.containsValue(value)) {
            throw IllegalArgumentException("Tried to register duplicate translator under two different IDs! (new: $id, existing: ${this.translators.filterValues { it == value }.keys.first()})")
        }

        this.translators[id] = value
        this.translatorConfigs[id] = SunsetConfig.create(UnityTranslateApi.instance.configPath.resolve("translators/$id.json")) {
            val builder = SunsetWrappedConfigBuilder(this)
            configBuilder.invoke(builder)
        }
    }

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

    override fun getTranscriber(id: String): SpeechTranscriber {
        return this.transcribers[id] ?: InactiveTranscriber
    }

    fun getTranslator(id: String): TranslatorInstance {
        return this.translators[id] ?: InactiveTranslatorInstance
    }

    fun getTranslatorId(translator: TranslatorInstance): String {
        return this.translators.filterValues { it == translator }.keys.first()
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

    override fun registerOutputLanguage(id: String): LanguageHolder {
        if (this.outputLanguages.contains(id))
            throw IllegalArgumentException("An output language already exists by ID $id!")

        val holder = LanguageHolder()
        this.outputLanguages[id] = holder
        return holder
    }
}
