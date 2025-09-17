package xyz.bluspring.unitytranslate.transcriber.api

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.config.ConfigBuilder

/**
 * An entrypoint that allows registering a new transcriber into UnityTranslate.
 */
interface SpeechTranscriberEntrypoint {
    fun createTranscriber(language: Language): SpeechTranscriber

    fun addToConfig(builder: ConfigBuilder) {}
}