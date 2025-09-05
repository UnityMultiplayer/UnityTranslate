package xyz.bluspring.unitytranslate.minecraft.compat.talk_balloons

import com.cerbon.talk_balloons.api.TalkBalloonsApi
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.common.events.TranscriptEvents
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

object TalkBalloonsCompat {
    private val lastBalloonText = ConcurrentSkipListMap<UUID, Pair<Int, String>>()

    fun init() {
        TranscriptEvents.UPDATE.register { transcript, language ->
            val balloonLanguage = UnityTranslateMCClient.clientConfig.balloonLanguage ?: UnityTranslateMCClient.clientConfig.spokenLanguage
            if (language != balloonLanguage)
                return@register

            val uuid = transcript.player
            val player = transcript.playerRef

            if (player !is Player)
                return@register

            val balloonMessages = TalkBalloonsApi.INSTANCE.getBalloonMessages(player)

            if (balloonMessages.isEmpty()) {
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

            TalkBalloonsApi.INSTANCE.createBalloonMessage(player, text)
            lastBalloonText[uuid] = transcript.index to text
        }
    }
}