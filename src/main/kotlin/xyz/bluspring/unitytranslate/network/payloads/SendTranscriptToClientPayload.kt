package xyz.bluspring.unitytranslate.network.payloads

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketDefinitions
import java.util.*

data class SendTranscriptToClientPayload(
    val uuid: UUID,
    val language: Language,
    val index: Int,
    val updateTime: Long,
    val toSend: Map<Language, String>
) : NetworkPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = PacketDefinitions.SEND_TRANSCRIPT_TO_CLIENT

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.UUID, SendTranscriptToClientPayload::uuid,
            Language.NETWORK_CODEC, SendTranscriptToClientPayload::language,
            NetworkCodecs.VAR_INT, SendTranscriptToClientPayload::index,
            NetworkCodecs.VAR_LONG, SendTranscriptToClientPayload::updateTime,
            NetworkCodecs.createMap(Language.NETWORK_CODEC, NetworkCodecs.STRING_UTF8), SendTranscriptToClientPayload::toSend,

            ::SendTranscriptToClientPayload
        )
    }
}