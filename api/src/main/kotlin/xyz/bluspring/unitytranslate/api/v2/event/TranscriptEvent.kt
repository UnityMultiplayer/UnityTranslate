package xyz.bluspring.unitytranslate.api.v2.event

import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder

@ApiStatus.NonExtendable
fun interface TranscriptEvent {
    fun onTranscriptEvent(holder: TranscriptHolder)

    companion object {
        @JvmField val UPDATED = Event(TranscriptUpdatedEvent::class.java) { events ->
            TranscriptUpdatedEvent { holder, data ->
                for (event in events) {
                    event.onTranscriptUpdated(holder, data)
                }
            }
        }

        private fun create(): Event<TranscriptEvent> = Event(TranscriptEvent::class.java) { events ->
            TranscriptEvent { holder ->
                for (event in events) {
                    event.onTranscriptEvent(holder)
                }
            }
        }
    }

    @ApiStatus.NonExtendable
    fun interface TranscriptUpdatedEvent {
        fun onTranscriptUpdated(holder: TranscriptHolder, data: TranscriptData)
    }
}
