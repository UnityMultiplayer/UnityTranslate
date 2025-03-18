package xyz.bluspring.unitytranslate.common.transcriber

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate

enum class TranscriberType(val enabled: Boolean = true) {
    SPHINX(false),
    BROWSER,
    WINDOWS_SAPI,
    WHISPER;

    lateinit var creator: (UnityTranslate, Language) -> SpeechTranscriber

    val isAvailable: Boolean
        get() = this.enabled && this::creator.isInitialized
}