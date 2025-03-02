package xyz.bluspring.unitytranslate.minecraft.events

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.common.util.Event

interface TranscriptEvents {
    fun interface Update {
        fun onTranscriptUpdate(transcript: Transcript, language: Language)
    }

    companion object {
        @JvmField val UPDATE: Event<Update> = Event(Update::class.java) { events ->
            Update { transcript, language ->
                for (event in events) {
                    event.onTranscriptUpdate(transcript, language)
                }
            }
        }
    }
}