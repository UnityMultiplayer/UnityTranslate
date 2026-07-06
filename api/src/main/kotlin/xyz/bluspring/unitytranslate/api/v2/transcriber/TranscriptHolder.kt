package xyz.bluspring.unitytranslate.api.v2.transcriber

import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData.Companion.id
import java.util.*

/**
 * A transcript holder, holding a list of [transcripts]. Used by both transcript boxes and the Talk Balloons integration.
 */
@JvmRecord
data class TranscriptHolder @ApiStatus.Internal constructor(
    /**
     * The language that is used by this transcript holder.
     * This means that transcripts will be translated to this language when provided,
     * if it's not already in this language.
     */
    val language: Language,

    /**
     * The list of transcripts that are in this transcript holder.
     * Note that transcripts will be automatically removed after either a user-configurable amount of time
     * or a user-configurable amount has been reached.
     */
    val transcripts: MutableCollection<TranscriptData> = Collections.synchronizedList(mutableListOf()),
) {
    fun update(data: TranscriptData) {
        synchronized(this.transcripts) {
            this.transcripts.removeIf { it.id == data.id }
            this.transcripts.add(data)
        }

        TranscriptEvent.UPDATED.invoker().onTranscriptUpdated(this, data)
    }
}
