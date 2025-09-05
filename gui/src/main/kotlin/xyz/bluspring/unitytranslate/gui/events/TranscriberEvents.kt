package xyz.bluspring.unitytranslate.gui.events

import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.common.util.Event

object TranscriberEvents {
    @JvmField val TRANSCRIBER_STARTED = createGenericEvent()
    @JvmField val TRANSCRIBER_STOPPED = createGenericEvent()
    @JvmField val TRANSCRIBER_ERROR = Event(TranscriptErrorEvent::class.java) { callbacks ->
        TranscriptErrorEvent { type, error ->
            for (callback in callbacks) {
                callback.onTranscriptError(type, error)
            }
        }
    }

    private fun createGenericEvent(): Event<GenericTranscriptEvent> {
        return Event(GenericTranscriptEvent::class.java) { callbacks ->
            GenericTranscriptEvent { type ->
                for (callback in callbacks) {
                    callback.onTranscriptEvent(type)
                }
            }
        }
    }

    fun interface GenericTranscriptEvent {
        fun onTranscriptEvent(type: TranscriberType)
    }

    fun interface TranscriptErrorEvent {
        fun onTranscriptError(type: TranscriberType, error: Throwable)
    }
}