package xyz.bluspring.unitytranslate.api.v2

import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder

data class LanguageHolder @ApiStatus.Internal constructor (
    var languageOrNull: Language? = null,
) {
    /**
     * Warning: Do NOT hold a reference to this! This holder may get updated at any time if the language code changes!
     */
    val transcriptHolder: TranscriptHolder
        get() = UnityTranslateApi.instance.getOrCreateTranscriptHolder(this.language)

    var language: Language
        get() = this.languageOrNull ?: UnityTranslateApi.instance.currentSpokenLanguage
        set(value) {
            this.languageOrNull = value
        }

    val isDefault: Boolean
        get() = this.languageOrNull == null

    fun reset() {
        this.languageOrNull = null
    }
}
