package xyz.bluspring.unitytranslate.client.config

import xyz.bluspring.unitytranslate.api.v2.transcriber.SpeechTranscriber

data class ClientConfig(
    val language: LanguageConfig,
    val transcriptBoxes: MutableList<TranscriptBoxConfig>,
    var transcriber: SpeechTranscriber,
) {
    data class LanguageConfig(
        var spoken: String,
        var balloon: String,
    )
}
