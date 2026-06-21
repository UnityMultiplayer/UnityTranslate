package xyz.bluspring.unitytranslate

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.config.ClientConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.plugin.PluginManager
import xyz.bluspring.unitytranslate.shared.Constants
import xyz.bluspring.unitytranslate.translator.TranslatorManagerImpl
import xyz.bluspring.unitytranslate.translator.instance.UnityTranslateLibTranslatorInstance

object UnityTranslate {
    const val MOD_ID = Constants.MOD_ID
    val logger: Logger = LoggerFactory.getLogger("UnityTranslate")

    fun init() {
        UnityTranslateApi.instance.registerConfig("unitytranslate") {
            category("client") {
                category("default_box_settings") {
                    intColor("text", TranscriptBoxConfig.Defaults::textColor)
                    intColor("shadow", TranscriptBoxConfig.Defaults::shadowColor)

                    value("outline", TranscriptBoxConfig.Outline.CODEC, TranscriptBoxConfig.Defaults::outline)
                    value("background", TranscriptBoxConfig.Background.CODEC, TranscriptBoxConfig.Defaults::background)
                    value("transcript_display", TranscriptBoxConfig.TranscriptDisplay.CODEC, TranscriptBoxConfig.Defaults::transcriptDisplay)

                    category("header") {
                        value("display", TranscriptBoxConfig.HeaderDisplay.CODEC, TranscriptBoxConfig.Defaults.header::display, TranscriptBoxConfig.Defaults.header)
                        value("style", Style.Serializer.CODEC, TranscriptBoxConfig.Defaults.header::style, TranscriptBoxConfig.Defaults.header)
                        value("lang_display", TranscriptBoxConfig.LanguageDisplay.CODEC, TranscriptBoxConfig.Defaults.header::langDisplay, TranscriptBoxConfig.Defaults.header)
                        value("lang_style", Style.Serializer.CODEC, TranscriptBoxConfig.Defaults.header::langStyle, TranscriptBoxConfig.Defaults.header)
                        value("lang_decoration", TranscriptBoxConfig.LanguageDecoration.CODEC, TranscriptBoxConfig.Defaults.header::langDecoration, TranscriptBoxConfig.Defaults.header)
                        value("align_x", TranscriptBoxConfig.Header.HorizontalAlignment.CODEC, TranscriptBoxConfig.Defaults.header::alignX, TranscriptBoxConfig.Defaults.header)
                        value("align_y", TranscriptBoxConfig.Header.VerticalAlignment.CODEC, TranscriptBoxConfig.Defaults.header::alignY, TranscriptBoxConfig.Defaults.header)
                        boolean("has_shadow", TranscriptBoxConfig.Defaults.header::hasShadow, TranscriptBoxConfig.Defaults.header)
                    }

                    category("padding") {
                        float("left", 0f, 32f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::left, owner = TranscriptBoxConfig.Defaults.padding) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }

                        float("right", 0f, 32f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::right, owner = TranscriptBoxConfig.Defaults.padding) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }

                        float("top", 0f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::top, owner = TranscriptBoxConfig.Defaults.padding) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }

                        float("bottom", 0f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::bottom, owner = TranscriptBoxConfig.Defaults.padding) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }
                    }

                    float("corner_radius", min = 0f, max = 16f, step = 0.5f, property = TranscriptBoxConfig.Defaults::cornerRadius) {
                        formatting {
                            Component.literal("${"%.1f".format(it)} px")
                        }
                    }

                    float("font_scale", min = 0f, max = 16f, step = 0.5f, property = TranscriptBoxConfig.Defaults::fontScale) {
                        formatting {
                            if (it == 0f)
                                Component.literal("(default)")
                            else
                                Component.literal("${"%.1f".format(it * 100f)}%")
                        }
                    }
                }

                category("language") {
                    string("spoken", ClientConfig::spokenLanguage)
                }

                value("transcriber", SpeechTranscriber.CODEC, ClientConfig::transcriber)
                listValue("transcript_boxes", TranscriptBoxConfig.CODEC, ClientConfig::transcriptBoxes)
            }

            category("common") {
                category("translator") {
                    integer("max_threads", 1, Runtime.getRuntime().availableProcessors(), 1, TranslatorManagerImpl.Config::maxThreads)
                    integer("batch_size", 1, 50, 1, TranslatorManagerImpl.Config::batchSize)
                    integer("delay_between_batches", 0, 5000, 250, TranslatorManagerImpl.Config::delayBetweenBatches)
                }
            }
        }

        UnityTranslateApi.instance.registerTranslator("unitytranslatelib", UnityTranslateLibTranslatorInstance) {
            boolean("enable_gpu", UnityTranslateLibTranslatorInstance::enableGpu)
        }

        PluginManager.loadPlugins()
    }

    @JvmStatic
    fun id(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(MOD_ID, path)
    }
}
