package xyz.bluspring.unitytranslate.client

import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.client.config.ClientConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig

object UnityTranslateClient {
    val config = SunsetConfig.create(UnityTranslateApi.instance.configPath.resolve("unitytranslate_client.json")) {
        category("language") {
            string("spoken", ClientConfig.language::spoken)
            string("balloon", ClientConfig.language::balloon)
        }

        category("transcriber") {
            value("type", SpeechTranscriber.CODEC, ClientConfig::transcriber)
        }

        value("transcript_boxes", TranscriptBoxConfig.CODEC.listOf().xmap({ it.toMutableList() }, { it.toMutableList() }), ClientConfig::transcriptBoxes)
    }

    fun init() {
        this.config.load()
    }
}
