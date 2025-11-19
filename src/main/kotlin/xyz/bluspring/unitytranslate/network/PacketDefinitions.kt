package xyz.bluspring.unitytranslate.network

import xyz.bluspring.modernnetworking.api.minecraft.VanillaNetworkRegistry
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.network.payloads.*

object PacketDefinitions {
    val registry = VanillaNetworkRegistry.create(UnityTranslate.MOD_ID)

    // Clientbound
    val SERVER_SUPPORT = registry.registerClientbound("server_support", ServerSupportPayload.CODEC)
    val SEND_TRANSCRIPT_TO_CLIENT = registry.registerClientbound("send_transcript_client", SendTranscriptToClientPayload.CODEC)
    val MARK_INCOMPLETE = registry.registerClientbound("mark_incomplete", MarkIncompletePayload.CODEC)

    // Serverbound
    val SEND_TRANSCRIPT_TO_SERVER = registry.registerServerbound("send_transcript_server", SendTranscriptToServerPayload.CODEC)
    val SET_USED_LANGUAGES = registry.registerServerbound("set_used_languages", SetUsedLanguagesPayload.CODEC)
    val SET_CURRENT_LANGUAGE = registry.registerServerbound("set_current_language", SetCurrentLanguagePayload.CODEC)

    fun init() {}
}