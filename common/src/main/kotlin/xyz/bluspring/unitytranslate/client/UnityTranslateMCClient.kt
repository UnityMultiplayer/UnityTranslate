package xyz.bluspring.unitytranslate.client

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.client.transcriber.sender.MinecraftLocalTranscriptUser

object UnityTranslateMCClient {
    val transcriberSource = UnityTranslateApi.instance.getOrCreateTranscriberSource(MinecraftLocalTranscriptUser)
    var serverSupportsTranslator = false

    fun init() {}
}
