package xyz.bluspring.unitytranslate.integration.voicechat

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent
import de.maxhenkel.voicechat.api.events.ClientSoundEvent
import de.maxhenkel.voicechat.api.events.EventRegistration
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.PlayerSender
import xyz.bluspring.unitytranslate.api.v2.util.AudioConverters
import xyz.bluspring.unitytranslate.client.UnityTranslateMCClient

@ForgeVoicechatPlugin
class SimpleVoiceChatIntegration : VoicechatPlugin {
    override fun getPluginId(): String = UnityTranslate.MOD_ID

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound::class.java) { event ->
            // We don't want to transcribe players we're not supposed to have.
            if (UnityTranslateMCClient.serverSupportsTranslator)
                return@registerEvent

            val uuid = event.entityId
            val entity = Minecraft.getInstance().level?.getEntity(uuid) ?: return@registerEvent

            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
            UnityTranslateApi.instance.getOrCreateTranscriberSource(PlayerSender(entity.uuid, entity.displayName))
                .submitSpeechSamples(samples)
        }

        registration.registerEvent(ClientSoundEvent::class.java) { event ->
            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
            UnityTranslateMCClient.transcriberSource.submitSpeechSamples(samples)
        }
    }
}
