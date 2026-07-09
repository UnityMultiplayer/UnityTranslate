package xyz.bluspring.unitytranslate.integration.talk_balloons

import com.cerbon.talk_balloons.api.TalkBalloonsApi
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.PlayerUser

object TalkBalloonsIntegration {
    fun setup() {
        val balloonLanguage = UnityTranslateApi.instance.registerOutputLanguage("balloon")

        TranscriptEvent.UPDATED.register { holder, data ->
            if (holder == balloonLanguage.transcriptHolder && data.sender is PlayerUser) {
                val player = Minecraft.getInstance().level?.getPlayerByUUID((data.sender as PlayerUser).uuid)
                    ?: return@register

                TalkBalloonsApi.INSTANCE.createBalloonMessage(player, data.message)
            }
        }
    }
}
