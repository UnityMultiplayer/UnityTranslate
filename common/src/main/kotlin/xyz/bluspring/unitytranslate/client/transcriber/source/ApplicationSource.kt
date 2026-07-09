package xyz.bluspring.unitytranslate.client.transcriber.source

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptSender

class ApplicationSource(val applicationName: String) : TranscriptSender {
    override val displayName: Component
        get() = Component.literal(this.applicationName)

    val source = UnityTranslateApi.instance.getOrCreateTranscriberSource(this)
}
