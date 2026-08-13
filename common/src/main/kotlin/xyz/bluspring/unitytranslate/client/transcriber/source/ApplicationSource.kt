package xyz.bluspring.unitytranslate.client.transcriber.source

import org.joml.Vector3f
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser

class ApplicationSource(val applicationName: String) : TranscriptUser {
    override val displayName: TextComponent
        get() = TextComponent.literal(this.applicationName)

    val source = UnityTranslateApi.instance.getOrCreateTranscriberSource(this)

    override val pos: Vector3f?
        get() = null
}
