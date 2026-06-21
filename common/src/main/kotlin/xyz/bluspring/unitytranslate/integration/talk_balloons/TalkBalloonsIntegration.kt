package xyz.bluspring.unitytranslate.integration.talk_balloons

import com.cerbon.talk_balloons.api.TalkBalloonsApi
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.PlayerSender

object TalkBalloonsIntegration {
    fun setup() {
        val balloonLanguage = UnityTranslateApi.instance.registerOutputLanguage("balloon")

        TranscriptEvent.UPDATED.register { holder, data ->
            if (holder == balloonLanguage.transcriptHolder && data.sender is PlayerSender) {
                val player = Minecraft.getInstance().level?.getPlayerByUUID((data.sender as PlayerSender).uuid)
                    ?: return@register

                TalkBalloonsApi.INSTANCE.createBalloonMessage(player, data.message)
            }
        }
    }
}
