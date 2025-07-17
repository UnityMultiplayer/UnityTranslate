package xyz.bluspring.unitytranslate.common.network.v1.clientbound

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets
import java.util.UUID

data class V1ClientboundSendTranscriptPacket(
    val uuid: UUID,
    val sourceLanguage: Language,
    val index: Int,
    val updateTime: Long,
    val toSend: Map<Language, String>
) : UTPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.SEND_TRANSCRIPT_CLIENTBOUND

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.UUID, V1ClientboundSendTranscriptPacket::uuid,
            Language.NETWORK_CODEC, V1ClientboundSendTranscriptPacket::sourceLanguage,
            NetworkCodecs.VAR_INT, V1ClientboundSendTranscriptPacket::index,
            NetworkCodecs.VAR_LONG, V1ClientboundSendTranscriptPacket::updateTime,
            NetworkCodecs.createMap(Language.NETWORK_CODEC, NetworkCodecs.STRING_UTF8), V1ClientboundSendTranscriptPacket::toSend,
            ::V1ClientboundSendTranscriptPacket
        )
    }
}