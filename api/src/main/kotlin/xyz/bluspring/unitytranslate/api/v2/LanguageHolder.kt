package xyz.bluspring.unitytranslate.api.v2

import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder

data class LanguageHolder @ApiStatus.Internal constructor (
    private var actualLanguageCode: String? = null,
) {
    /**
     * Warning: Do NOT hold a reference to this! This holder may get updated at any time if the language code changes!
     */
    val transcriptHolder: TranscriptHolder
        get() = UnityTranslateApi.instance.getOrCreateTranscriptHolder(this.languageCode)

    var languageCode: String
        get() = this.actualLanguageCode ?: UnityTranslateApi.instance.currentSpokenLanguage
        set(value) {
            this.actualLanguageCode = value
        }

    val isDefault: Boolean
        get() = this.actualLanguageCode == null

    fun reset() {
        this.actualLanguageCode = null
    }
}
