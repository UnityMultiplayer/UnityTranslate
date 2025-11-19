package xyz.bluspring.unitytranslate.transcript

import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.compat.voicechat.UTVoiceChatCompat
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import java.util.concurrent.ConcurrentLinkedQueue

class TranscriptHolder(val language: Language) {
    val transcripts = ConcurrentLinkedQueue<Transcript>()

    fun updateTranscript(source: Player, text: String, language: Language, index: Int, updateTime: Long, incomplete: Boolean) {
        if (!UTVoiceChatCompat.isPlayerAudible(source))
            return

        if (this.transcripts.any { it.player.uuid == source.uuid && it.index == index }) {
            val transcript = this.transcripts.first { it.player.uuid == source.uuid && it.index == index }

            // it's possible for this to go out of order, let's avoid that
            if (transcript.lastUpdateTime > updateTime)
                return

            transcript.lastUpdateTime = updateTime
            transcript.text = text
            transcript.incomplete = incomplete
            transcript.arrivalTime = System.currentTimeMillis()

            TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(transcript, this.language)

            return
        }

        this.transcripts.add(Transcript(index, source, text, language, updateTime, incomplete).apply {
            TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(this, this@TranscriptHolder.language)
        })
    }
}
