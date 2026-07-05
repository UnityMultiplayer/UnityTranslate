package xyz.bluspring.unitytranslate.client

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.client.transcriber.sender.MinecraftLocalTranscriptSender

object UnityTranslateMCClient {
    val transcriberSource = UnityTranslateApi.instance.getOrCreateTranscriberSource(MinecraftLocalTranscriptSender)
    var serverSupportsTranslator = false

    fun init() {}
}
