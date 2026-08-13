package xyz.bluspring.unitytranslate.integration.voicechat

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent
import de.maxhenkel.voicechat.api.events.ClientSoundEvent
import de.maxhenkel.voicechat.api.events.EventRegistration
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.PlayerUser
import xyz.bluspring.unitytranslate.api.v2.util.AudioConverters
import xyz.bluspring.unitytranslate.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.transcriber.TranscriberSourceImpl
import xyz.bluspring.unitytranslate.util.PlatformConversion.asUnityTranslate
import kotlin.time.Duration.Companion.seconds

@ForgeVoicechatPlugin
class SimpleVoiceChatIntegration : VoicechatPlugin {
    override fun getPluginId(): String = UnityTranslate.MOD_ID

    override fun initialize(api: VoicechatApi) {
        super.initialize(api)

        TranscriptEvent.ALLOWED.register { _, data, receiver ->
            val senderPos = data.sender.pos ?: return@register true
            val receiverPos = receiver.pos ?: return@register true

            senderPos.distanceSquared(receiverPos) <= api.voiceChatDistance * api.voiceChatDistance
        }
    }

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound::class.java) { event ->
            // We don't want to transcribe players we're not supposed to have.
            if (UnityTranslateMCClient.serverSupportsTranslator)
                return@registerEvent

            val uuid = event.entityId
            val entity = Minecraft.getInstance().level?.getEntity(uuid) ?: return@registerEvent
            val source = UnityTranslateApi.instance.getOrCreateTranscriberSource(PlayerUser(entity.uuid, entity.position().toVector3f(), entity.displayName.asUnityTranslate()))

            if (System.currentTimeMillis() - (source as TranscriberSourceImpl).lastUpdateTimestamp >= 2.seconds.inWholeMilliseconds)
                source.reset()

            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
            source.submitSpeechSamples(AudioHelper.downsample48kTo16k(samples))
        }

        registration.registerEvent(ClientSoundEvent::class.java) { event ->
            val source = UnityTranslateMCClient.transcriberSource

            if (System.currentTimeMillis() - (source as TranscriberSourceImpl).lastUpdateTimestamp >= 2.seconds.inWholeMilliseconds)
                source.reset()

            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
            UnityTranslateMCClient.transcriberSource.submitSpeechSamples(AudioHelper.downsample48kTo16k(samples))
        }
    }
}
