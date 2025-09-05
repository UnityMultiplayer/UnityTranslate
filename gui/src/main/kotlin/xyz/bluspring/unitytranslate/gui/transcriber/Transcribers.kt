package xyz.bluspring.unitytranslate.gui.transcriber

import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.gui.transcriber.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.gui.transcriber.whisper.WhisperTranscriber

object Transcribers {
    fun init() {
        TranscriberType.BROWSER.creator = ::BrowserSpeechTranscriber
        WhisperTranscriber.tryInitialize()
    }
}