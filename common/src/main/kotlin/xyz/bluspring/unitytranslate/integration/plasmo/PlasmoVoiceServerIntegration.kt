package xyz.bluspring.unitytranslate.integration.plasmo

import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.server.PlasmoVoiceServer
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.PlayerUser

@Addon(
    id = UnityTranslate.MOD_ID,
    name = "UnityTranslate",
    version = "2.0.0",
    authors = ["BluSpring"]
)
class PlasmoVoiceServerIntegration : AddonInitializer {
    @InjectPlasmoVoice
    private lateinit var voiceServer: PlasmoVoiceServer

    override fun onAddonInitialize() {
        TranscriptEvent.ALLOWED.register { _, data, receiver ->
            val senderPos = data.sender.pos ?: return@register true
            val receiverPos = receiver.pos ?: return@register true

            if (receiver is PlayerUser) {
                val receiverPlayer = voiceServer.playerManager.getPlayerById(receiver.uuid)
                if (receiverPlayer.isPresent) {
                    val player = receiverPlayer.orElseThrow()

                    if (player.isVoiceDisabled)
                        return@register false
                }
            } else if (data.sender is PlayerUser) {
                val senderPlayer = voiceServer.playerManager.getPlayerById((data.sender as PlayerUser).uuid)
                if (senderPlayer.isPresent) {
                    val player = senderPlayer.orElseThrow()

                    if (player.isMicrophoneMuted)
                        return@register false
                }

                voiceServer
            }

            val distance = voiceServer.config?.voice()?.proximity()?.defaultDistance() ?: 5
            senderPos.distanceSquared(receiverPos) <= distance * distance
        }
    }
}
