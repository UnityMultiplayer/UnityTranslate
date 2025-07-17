package xyz.bluspring.unitytranslate.common.network.v1.serverbound

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets

data class V1ServerboundSendTranscriptPacket(
    val sourceLanguage: Language,
    val index: Int,
    val updateTime: Long,
    val text: String
) : UTPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.SEND_TRANSCRIPT_SERVERBOUND

    companion object {
        val CODEC = CompositeCodecs.composite(
            Language.NETWORK_CODEC, V1ServerboundSendTranscriptPacket::sourceLanguage,
            NetworkCodecs.VAR_INT, V1ServerboundSendTranscriptPacket::index,
            NetworkCodecs.VAR_LONG, V1ServerboundSendTranscriptPacket::updateTime,
            NetworkCodecs.STRING_UTF8, V1ServerboundSendTranscriptPacket::text,
            ::V1ServerboundSendTranscriptPacket
        )
    }
}