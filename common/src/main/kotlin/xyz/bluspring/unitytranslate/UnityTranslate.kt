package xyz.bluspring.unitytranslate

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.transcriber.InactiveTranscriber
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.config.ClientConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.config.builders.SunsetWrappedConfigBuilder
import xyz.bluspring.unitytranslate.config.values.HiddenReflectingConfigValue
import xyz.bluspring.unitytranslate.integration.UnityTranslateIntegration
import xyz.bluspring.unitytranslate.plugin.PluginManager
import xyz.bluspring.unitytranslate.shared.Constants
import xyz.bluspring.unitytranslate.translator.TranslatorManagerImpl
import xyz.bluspring.unitytranslate.translator.instance.InactiveTranslatorInstance
import xyz.bluspring.unitytranslate.translator.instance.LibreTranslateTranslatorInstance
import xyz.bluspring.unitytranslate.translator.instance.UnityTranslateLibTranslatorInstance

object UnityTranslate {
    const val MOD_ID = Constants.MOD_ID
    val logger: Logger = LoggerFactory.getLogger("UnityTranslate")

    fun init() {
        UnityTranslateApi.instance.registerTranscriber("inactive", InactiveTranscriber) {}
        UnityTranslateApi.instance.registerTranslator("inactive", InactiveTranslatorInstance) {}

        UnityTranslateApi.instance.registerTranslator("unitytranslatelib", UnityTranslateLibTranslatorInstance) {
            boolean("enable_gpu", UnityTranslateLibTranslatorInstance::enableGpu)
        }

        UnityTranslateApi.instance.registerTranslator("libretranslate", LibreTranslateTranslatorInstance) {
            listValue("entries", LibreTranslateTranslatorInstance.Entry.CODEC, LibreTranslateTranslatorInstance::entries)
        }

        UnityTranslateIntegration.setup()
        PluginManager.loadPlugins()

        UnityTranslateApi.instance.registerConfig("unitytranslate") {
            category("hud") {
                category("default_box_settings") {
                    intColor("text", TranscriptBoxConfig.Defaults::textColor)
                    intColor("shadow", TranscriptBoxConfig.Defaults::shadowColor)

                    value("outline", TranscriptBoxConfig.Outline.CODEC, TranscriptBoxConfig.Defaults::outline)
                    value("background", TranscriptBoxConfig.Background.CODEC, TranscriptBoxConfig.Defaults::background)
                    value("transcript_display", TranscriptBoxConfig.TranscriptDisplay.CODEC, TranscriptBoxConfig.Defaults::transcriptDisplay)

                    category("header") {
                        value("display", TranscriptBoxConfig.HeaderDisplay.CODEC, TranscriptBoxConfig.Defaults.header::display)
                        value("style", Style.Serializer.CODEC, TranscriptBoxConfig.Defaults.header::style)
                        value("lang_display", TranscriptBoxConfig.LanguageDisplay.CODEC, TranscriptBoxConfig.Defaults.header::langDisplay)
                        value("lang_style", Style.Serializer.CODEC, TranscriptBoxConfig.Defaults.header::langStyle)
                        value("lang_decoration", TranscriptBoxConfig.LanguageDecoration.CODEC, TranscriptBoxConfig.Defaults.header::langDecoration)
                        value("align_x", TranscriptBoxConfig.Header.HorizontalAlignment.CODEC, TranscriptBoxConfig.Defaults.header::alignX)
                        value("align_y", TranscriptBoxConfig.Header.VerticalAlignment.CODEC, TranscriptBoxConfig.Defaults.header::alignY)
                        boolean("has_shadow", TranscriptBoxConfig.Defaults.header::hasShadow)
                    }

                    category("padding") {
                        float("left", 0f, 32f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::left) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }

                        float("right", 0f, 32f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::right) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }

                        float("top", 0f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::top) {
                            formatting {
                                Component.literal("${"%.1f".format(it)} px")
                            }
                        }

                        float("bottom", 0f, step = 0.1f, property = TranscriptBoxConfig.Defaults.padding::bottom) {
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

                listValue("transcript_boxes", TranscriptBoxConfig.CODEC, ClientConfig::transcriptBoxes)
            }

            value("transcriber", SpeechTranscriber.CODEC, ClientConfig::transcriber)

            category("languages") {
                string("spoken", ClientConfig::spokenLanguage)

                for ((langKey, langHolder) in UnityTranslateApiImpl.outputLanguages) {
                    value(langKey, Language.CODEC, langHolder::language)
                }
            }

            category("translator") {
                integer("max_threads", 1, Runtime.getRuntime().availableProcessors(), 1, TranslatorManagerImpl.Config::maxThreads)
                integer("batch_size", 1, 50, 1, TranslatorManagerImpl.Config::batchSize)
                integer("delay_between_batches", 0, 5000, 250, TranslatorManagerImpl.Config::delayBetweenBatches)

                listValue("instances", Codec.STRING.dispatch("type", { UnityTranslateApiImpl.getTranslatorId(it) }, {
                    MapCodec.unit { UnityTranslateApiImpl.getTranslator(it) }
                }), TranslatorManagerImpl::instances)
            }

            if (this is SunsetWrappedConfigBuilder) {
                this.wrapped.custom(HiddenReflectingConfigValue("handled_first_join", Codec.BOOL, UnityTranslateClient::handledFirstJoin))
            }
        }

        TranslatorManagerImpl.startTicking()
    }

    @JvmStatic
    fun id(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(MOD_ID, path)
    }
}
