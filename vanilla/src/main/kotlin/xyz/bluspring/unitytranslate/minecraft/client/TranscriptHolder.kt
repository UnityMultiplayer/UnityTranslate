package xyz.bluspring.unitytranslate.minecraft.client

import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.minecraft.events.TranscriptEvents
import java.util.concurrent.ConcurrentLinkedQueue

class TranscriptHolder(val language: Language) {
    val transcripts = ConcurrentLinkedQueue<Transcript>()

    fun tick() {
        val currentTime = System.currentTimeMillis()
        if (transcripts.size > 50) {
            for (i in 0..(transcripts.size - 50)) {
                transcripts.remove()
            }
        }

        val clientConfig = UnityTranslate.instance.config.client

        if (clientConfig.disappearingText) {
            for (transcript in transcripts) {
                if (currentTime >= (transcript.arrivalTime + (clientConfig.disappearingTextDelay * 1000L).toLong() + (clientConfig.disappearingTextFade * 1000L).toLong())) {
                    transcripts.remove(transcript)
                }
            }
        }
    }

    fun updateTranscript(source: Player, text: String, language: Language, index: Int, updateTime: Long, incomplete: Boolean) {
        if (UnityTranslate.instance.voiceChat != null && !UnityTranslate.instance.voiceChat!!.isPlayerAudible(source.uuid))
            return

        if (this.transcripts.any { it.player == source.uuid && it.index == index }) {
            val transcript = this.transcripts.first { it.player == source.uuid && it.index == index }

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

        this.transcripts.add(Transcript(index, source.uuid, text, language, updateTime, incomplete).apply {
            TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(this, this@TranscriptHolder.language)
        })
    }
}
