package xyz.bluspring.unitytranslate.integration.voicechat

import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent
import de.maxhenkel.voicechat.api.events.EventRegistration
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.util.AudioConverters

class UTSimpleVoiceChatIntegration : VoicechatPlugin {
    override fun getPluginId(): String = UnityTranslate.MOD_ID

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound::class.java) { event ->
            val samples = AudioConverters.shortPcm16ToFloat(event.rawAudio)

//            UnityTranslateApi.instance.activeTranscriber?.transcribeSamples(samples, "en")
        }
    }
}
