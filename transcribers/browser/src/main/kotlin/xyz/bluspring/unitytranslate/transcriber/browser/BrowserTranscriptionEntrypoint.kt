package xyz.bluspring.unitytranslate.transcriber.browser

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.transcriber.api.SpeechTranscriber
import xyz.bluspring.unitytranslate.transcriber.api.SpeechTranscriberEntrypoint

class BrowserTranscriptionEntrypoint : SpeechTranscriberEntrypoint {
    override fun createTranscriber(language: Language): SpeechTranscriber {
        return BrowserSpeechTranscriber(language)
    }
}