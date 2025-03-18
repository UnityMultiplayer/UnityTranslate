package xyz.bluspring.unitytranslate.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.common.compat.voicechat.PlasmoVoiceChatCompat
import xyz.bluspring.unitytranslate.common.compat.voicechat.SimpleVoiceChatCompat
import xyz.bluspring.unitytranslate.common.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.common.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.common.network.UTServerNetworking
import xyz.bluspring.unitytranslate.common.translator.TranslatorManager
import xyz.bluspring.unitytranslate.library.UnityTranslateLib
import java.nio.file.Path

class UnityTranslate(val path: Path) {
    val configFile = path.resolve("unitytranslate.json").toFile()
    val library = UnityTranslateLib(path)
    var proxy = PlatformProxy(this)
    lateinit var config: UnityTranslateConfig

    val translatorManager = TranslatorManager(this)
    val serverNetworking = UTServerNetworking(this)

    var voiceChat: UTVoiceChatCompat? = null

    init {
        instance = this
    }

    fun init() {
        library.load()
        loadConfig()
        translatorManager.loadFromConfig()

        voiceChat = if (proxy.isLoaded("voicechat"))
            SimpleVoiceChatCompat.Companion
        else if (proxy.isLoaded("plasmovoice"))
            PlasmoVoiceChatCompat.Companion
        else null
    }

    fun saveConfig() {
        try {
            if (!this.configFile.parentFile.exists())
                this.configFile.parentFile.mkdirs()

            if (!this.configFile.exists())
                this.configFile.createNewFile()

            val serialized = json.encodeToString(
                UnityTranslateConfig.serializer(),
                this.config
            )

            this.configFile.writeText(serialized)
        } catch (e: Exception) {
            logger.error("Failed to save UnityTranslate config!")
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadConfig() {
        if (!this.configFile.exists()) {
            config = UnityTranslateConfig()
            return
        }

        try {
            config = json.decodeFromStream(UnityTranslateConfig.serializer(), configFile.inputStream())
        } catch (e: Exception) {
            logger.error("Failed to load UnityTranslate config, reverting to defaults.")
            config = UnityTranslateConfig()
            e.printStackTrace()
        }
    }

    /**
     * Returns the server config and common config.
     */
    fun createSyncConfig(): Pair<String, String> {
        return Pair(
            networkJson.encodeToString(config.server),
            networkJson.encodeToString(config.common)
        )
    }

    companion object {
        const val MOD_ID = "unitytranslate"
        val json = Json {
            this.ignoreUnknownKeys = true
            this.prettyPrint = true
        }
        val networkJson = Json {
            this.encodeDefaults = true
            this.prettyPrint = false
        }

        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger("UnityTranslate")

        lateinit var instance: UnityTranslate
    }
}