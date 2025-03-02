package xyz.bluspring.unitytranslate.common.compat.voicechat

import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.client.event.connection.VoicePlayerUpdateEvent
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.PlasmoVoiceServer
import java.util.*

@Addon(
    id = "pv-unitytranslate-compat",
    name = "UnityTranslate",
    version = "1.0.0",
    authors = [ "BluSpring" ],
    scope = AddonLoaderScope.ANY
)
class PlasmoVoiceChatCompat : AddonInitializer {
    @InjectPlasmoVoice lateinit var voiceServer: PlasmoVoiceServer

    @EventSubscribe
    fun onVoiceUpdateEvent(event: VoicePlayerUpdateEvent) {
    }

    override fun onAddonInitialize() {
        plasmoVoice = this
    }

    companion object : UTVoiceChatCompat {
        lateinit var plasmoVoice: PlasmoVoiceChatCompat

        override val maxVoiceDistance: Double
            get() {
                return plasmoVoice.voiceServer.config?.voice()?.proximity()?.defaultDistance()?.toDouble() ?: 5.0
            }

        override fun isPlayerDeafened(player: UUID): Boolean {
            val vcPlayer = plasmoVoice.voiceServer.playerManager.getPlayerById(player).orElse(null) ?: return false

            if (!vcPlayer.hasVoiceChat())
                return true

            return vcPlayer.isVoiceDisabled
        }

        override fun isPlayerMutedOrDeafened(player: UUID): Boolean {
            val vcPlayer = plasmoVoice.voiceServer.playerManager.getPlayerById(player).orElse(null) ?: return false

            if (!vcPlayer.hasVoiceChat())
                return true

            return vcPlayer.isVoiceDisabled || vcPlayer.isMicrophoneMuted
        }

        override fun isPlayerAudible(player: UUID): Boolean {
            val connection = PlasmoVoiceClientCompat.instance.voiceClient.serverConnection.orElse(null) ?: return false
            val vcPlayer = connection.getPlayerById(player).orElse(null) ?: return false

            return !vcPlayer.isMuted && !vcPlayer.isMicrophoneMuted && !vcPlayer.isVoiceDisabled
        }

        override fun playerSharesGroup(player: UUID, other: UUID): Boolean {
            // TODO: Support pv-addon-groups
            return false
        }
    }
}