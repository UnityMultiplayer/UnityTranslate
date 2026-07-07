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
import xyz.bluspring.unitytranslate.transcriber.TranscriberSourceImpl
import kotlin.time.Duration.Companion.seconds

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
            val source = UnityTranslateApi.instance.getOrCreateTranscriberSource(PlayerSender(entity.uuid, entity.displayName))

            if (System.currentTimeMillis() - (source as TranscriberSourceImpl).lastUpdateTimestamp >= 2.seconds.inWholeMilliseconds)
                source.reset()

            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
            source
                .submitSpeechSamples(this.downsampleTo16k(samples))
        }

        registration.registerEvent(ClientSoundEvent::class.java) { event ->
            val source = UnityTranslateMCClient.transcriberSource

            if (System.currentTimeMillis() - (source as TranscriberSourceImpl).lastUpdateTimestamp >= 2.seconds.inWholeMilliseconds)
                source.reset()

            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
            UnityTranslateMCClient.transcriberSource.submitSpeechSamples(this.downsampleTo16k(samples))
        }
    }

    private fun downsampleTo16k(input: FloatArray): FloatArray {
        val outputLength = input.size / 3
        val output = FloatArray(outputLength)

        for (i in 0 until outputLength) {
            val base = i * 3
            val sum = input[base] + input[base + 1] + input[base + 2]
            output[i] = sum / 3f
        }

        return output
    }
}
