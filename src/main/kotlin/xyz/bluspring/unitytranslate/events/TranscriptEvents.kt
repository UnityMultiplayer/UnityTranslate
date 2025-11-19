package xyz.bluspring.unitytranslate.events

import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.transcript.Transcript
import xyz.bluspring.unitytranslate.util.Event

interface TranscriptEvents {
    fun interface Update {
        fun onTranscriptUpdate(transcript: Transcript, language: Language)
    }

    companion object {
        val UPDATE: Event<Update> = Event(Update::class.java) { callbacks -> Update { transcript, language ->
            for (update in callbacks) {
                update.onTranscriptUpdate(transcript, language)
            }
        } }
    }
}