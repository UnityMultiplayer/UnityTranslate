package xyz.bluspring.unitytranslate.network.payloads

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketDefinitions

data class SendTranscriptToServerPayload(
    val sourceLanguage: Language,
    val text: String,
    val index: Int,
    val updateTime: Long
) : NetworkPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = PacketDefinitions.SEND_TRANSCRIPT_TO_SERVER

    companion object {
        val CODEC = CompositeCodecs.composite(
            Language.NETWORK_CODEC, SendTranscriptToServerPayload::sourceLanguage,
            NetworkCodecs.STRING_UTF8, SendTranscriptToServerPayload::text,
            NetworkCodecs.VAR_INT, SendTranscriptToServerPayload::index,
            NetworkCodecs.VAR_LONG, SendTranscriptToServerPayload::updateTime,

            ::SendTranscriptToServerPayload
        )
    }
}