package xyz.bluspring.unitytranslate.compat.talkballoons

import com.cerbon.talk_balloons.api.TalkBalloonsApi
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.events.TranscriptEvents
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

object TalkBalloonsCompat {
    private val lastBalloonText = ConcurrentSkipListMap<UUID, Pair<Int, String>>()

    fun init() {
        TranscriptEvents.UPDATE.register { transcript, language ->
            if (language != UnityTranslate.config.client.balloonLanguage)
                return@register

            val uuid = transcript.player.uuid
            val balloonMessages = TalkBalloonsApi.INSTANCE.getBalloonMessages(transcript.player)

            if (balloonMessages?.isNotEmpty() != true) {
                lastBalloonText.remove(uuid)
            }

            val text = transcript.text

            if (lastBalloonText.containsKey(uuid)) {
                val (index, lastText) = lastBalloonText[uuid]!!

                if (lastText == text)
                    return@register

                if (index == transcript.index) {
                    balloonMessages.removeIf { it.string == lastText }
                }
            }

            TalkBalloonsApi.INSTANCE.createBalloonMessage(transcript.player, text, TalkBalloonsApi.INSTANCE.defaultDuration * 20)
            lastBalloonText[uuid] = transcript.index to text
        }
    }
}