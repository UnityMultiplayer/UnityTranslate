package xyz.bluspring.unitytranslate.common.transcriber

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum

enum class TranscriberType(val enabled: Boolean = true) : TranslatableEnum {
    SPHINX(false),
    BROWSER,
    WINDOWS_SAPI,
    WHISPER;

    lateinit var creator: (UnityTranslate, Language) -> SpeechTranscriber
    override val translationKey = "unitytranslate.transcriber.type.${this.name.lowercase()}"

    val isAvailable: Boolean
        get() = this.enabled && this::creator.isInitialized
}