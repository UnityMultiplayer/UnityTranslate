package xyz.bluspring.unitytranslate.transcriber.sphinx

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType

class SphinxSpeechTranscriber(instance: UnityTranslate, language: Language) : SpeechTranscriber(TranscriberType.SPHINX, instance, language) {
    init {
        throw IllegalStateException("PocketSphinx transcription is currently not supported!")
    }

    override fun stop() {
    }
}