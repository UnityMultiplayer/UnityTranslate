package xyz.bluspring.unitytranslate.common.transcriber

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.transcriber.browser.BrowserSpeechTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.sapi5.WindowsSpeechApiTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.sphinx.SphinxSpeechTranscriber

enum class TranscriberType(val creator: (UnityTranslate, Language) -> SpeechTranscriber, val enabled: Boolean = true) {
    SPHINX(::SphinxSpeechTranscriber, false),
    BROWSER(::BrowserSpeechTranscriber),
    WINDOWS_SAPI(::WindowsSpeechApiTranscriber, WindowsSpeechApiTranscriber.isSupported())
}