package xyz.bluspring.unitytranslate.transcriber

import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.transcriber.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.transcriber.whisper.WhisperTranscriber

object Transcribers {
    fun init() {
        TranscriberType.BROWSER.creator = ::BrowserSpeechTranscriber
        WhisperTranscriber.tryInitialize()
    }
}