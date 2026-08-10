package xyz.bluspring.unitytranslate.integration.plasmo

import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.event.audio.capture.AudioCaptureProcessedEvent
import su.plo.voice.api.event.EventPriority
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.client.AudioHelper
import xyz.bluspring.unitytranslate.api.v2.util.AudioConverters
import xyz.bluspring.unitytranslate.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.transcriber.TranscriberSourceImpl
import kotlin.time.Duration.Companion.seconds

@Addon(
    id = UnityTranslate.MOD_ID,
    name = "UnityTranslate",
    version = "2.0.0",
    authors = ["BluSpring"]
)
class PlasmoVoiceClientIntegration : AddonInitializer {
    @InjectPlasmoVoice
    private lateinit var voiceClient: PlasmoVoiceClient

    override fun onAddonInitialize() {
        this.voiceClient.eventBus.register(this, AudioCaptureProcessedEvent::class.java, EventPriority.NORMAL) { event ->
            val source = UnityTranslateMCClient.transcriberSource

            if (System.currentTimeMillis() - (source as TranscriberSourceImpl).lastUpdateTimestamp >= 2.seconds.inWholeMilliseconds)
                source.reset()

            val samples = AudioConverters.shortPcm16ToFloat(event.rawSamples)
            UnityTranslateMCClient.transcriberSource.submitSpeechSamples(AudioHelper.downsample48kTo16k(samples))
        }

        // TODO: how do we do this
//        this.voiceClient.eventBus.register(this, AudioSourceWriteEvent::class.java, EventPriority.NORMAL) { event ->
//            // We don't want to transcribe players we're not supposed to have.
//            if (UnityTranslateMCClient.serverSupportsTranslator)
//                return@register
//
//            val entity = Minecraft.getInstance().level?.getEntity(uuid) ?: return@register
//            val source = UnityTranslateApi.instance.getOrCreateTranscriberSource(PlayerUser(entity.uuid, entity.position().toVector3f(), entity.displayName))
//
//            if (System.currentTimeMillis() - (source as TranscriberSourceImpl).lastUpdateTimestamp >= 2.seconds.inWholeMilliseconds)
//                source.reset()
//
//            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)
//            source.submitSpeechSamples(AudioHelper.downsample48kTo16k(samples))
//        }
    }
}
