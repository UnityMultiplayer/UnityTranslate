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

data class V1MarkIncompletePacket(
    val uuid: UUID,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val index: Int,
    val isIncomplete: Boolean
) : UTPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.MARK_INCOMPLETE

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.UUID, V1MarkIncompletePacket::uuid,
            Language.NETWORK_CODEC, V1MarkIncompletePacket::sourceLanguage,
            Language.NETWORK_CODEC, V1MarkIncompletePacket::targetLanguage,
            NetworkCodecs.VAR_INT, V1MarkIncompletePacket::index,
            NetworkCodecs.BOOL, V1MarkIncompletePacket::isIncomplete,
            ::V1MarkIncompletePacket
        )
    }
}
