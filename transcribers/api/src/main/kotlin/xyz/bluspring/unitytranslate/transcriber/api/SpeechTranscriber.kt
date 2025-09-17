package xyz.bluspring.unitytranslate.transcriber.api

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate

abstract class SpeechTranscriber(open var language: Language) {
    // only god knows how this actually works now.
    protected var lastIndex = 0
    protected var currentOffset = 0

    /**
     * Converts the given language into the natively supported transcriber code.
     */
    abstract val Language.asTranscriberCode: String

    /**
     * Represents the languages supported by the transcriber.
     */
    abstract val supportedLanguages: Set<Language>

    /**
     * Sets the mute state of the transcriber.
     */
    abstract var isMuted: Boolean

    /**
     * The transcriber is stopped entirely. The transcriber instance must be recreated
     * in order to use the transcriber again.
     */
    abstract fun stop()
}