package xyz.bluspring.unitytranslate.common.transcriber

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import java.util.function.BiConsumer

abstract class SpeechTranscriber(val type: TranscriberType, val instance: UnityTranslate, var language: Language) {
    var lastIndex = 0
    var currentOffset = 0

    lateinit var updater: BiConsumer<Int, String>

    abstract fun stop()
    open fun setMuted(muted: Boolean) {}

    open fun changeLanguage(language: Language) {
        this.language = language
        instance.proxy.setClientPlayerLanguage(language)
    }
}