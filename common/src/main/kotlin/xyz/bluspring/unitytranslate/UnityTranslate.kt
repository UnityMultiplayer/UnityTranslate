package xyz.bluspring.unitytranslate

import com.mojang.serialization.Codec
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.config.ClientConfig
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.plugin.PluginManager
import xyz.bluspring.unitytranslate.shared.Constants
import kotlin.reflect.KMutableProperty

object UnityTranslate {
    const val MOD_ID = Constants.MOD_ID
    val logger: Logger = LoggerFactory.getLogger("UnityTranslate")

    private fun ConfigBuilder.colorConfig(id: String, property: KMutableProperty<ColorConfig>, owner: Any? = null) {
        category("color") {
            dropdown("type", ColorConfig.TYPES, Codec.STRING, )
            dispatch("type", config::type, config) {  ->
                when (type) {
                    "none" -> {}
                    "solid" -> {
                        intColor()
                    }
                }
            }
        }
    }

    fun init() {
        UnityTranslateApi.instance.registerConfig("unitytranslate") {
            category("client") {
                category("default_box_settings") {
                    intColor("text", TranscriptBoxConfig.Defaults::textColor)
                    intColor("shadow", TranscriptBoxConfig.Defaults::shadowColor)

                    category("outline") {

                    }
                }

                category("language") {
                    string("spoken", ClientConfig.language::spoken, ClientConfig.language)
                    string("balloon", ClientConfig.language::balloon, ClientConfig.language)
                }

                category("transcriber") {
                    value("type", SpeechTranscriber.CODEC, ClientConfig::transcriber)
                }

                value("transcript_boxes", TranscriptBoxConfig.CODEC.listOf().xmap({ it.toMutableList() }, { it.toMutableList() }), ClientConfig::transcriptBoxes)
            }

            category("common") {

            }
        }

        PluginManager.loadPlugins()
    }

    @JvmStatic
    fun id(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(MOD_ID, path)
    }
}
