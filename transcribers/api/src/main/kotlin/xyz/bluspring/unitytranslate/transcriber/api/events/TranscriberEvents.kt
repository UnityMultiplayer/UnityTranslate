package xyz.bluspring.unitytranslate.transcriber.api.events

import xyz.bluspring.unitytranslate.common.util.Event
import xyz.bluspring.unitytranslate.transcriber.api.SpeechTranscriber

object TranscriberEvents {
    @JvmField val STARTED = createGenericEvent()
    @JvmField val STOPPED = createGenericEvent()

    @JvmField val ERROR = Event(TranscriptErrorEvent::class.java) { callbacks ->
        TranscriptErrorEvent { transcriber, error ->
            for (callback in callbacks) {
                callback.onTranscriptError(transcriber, error)
            }
        }
    }

    @JvmField val UPDATED = Event(TranscriptUpdateEvent::class.java) { callbacks ->
        TranscriptUpdateEvent { transcriber, index, text ->
            for (callback in callbacks) {
                callback.onTranscriptUpdated(transcriber, index, text)
            }
        }
    }

    private fun createGenericEvent(): Event<GenericTranscriptEvent> {
        return Event(GenericTranscriptEvent::class.java) { callbacks ->
            GenericTranscriptEvent { transcriber ->
                for (callback in callbacks) {
                    callback.onTranscriptEvent(transcriber)
                }
            }
        }
    }

    fun interface GenericTranscriptEvent {
        fun onTranscriptEvent(transcriber: SpeechTranscriber)
    }

    fun interface TranscriptErrorEvent {
        fun onTranscriptError(transcriber: SpeechTranscriber, error: Throwable)
    }

    fun interface TranscriptUpdateEvent {
        fun onTranscriptUpdated(transcriber: SpeechTranscriber, index: Int, text: String)
    }
}