package xyz.bluspring.unitytranslate.common.network.v1.clientbound

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets

data class V1ResponseVersionPacket(
    val modVersion: String
) : UTPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.RESPONSE_VERSION

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.STRING_UTF8, V1ResponseVersionPacket::modVersion,
            ::V1ResponseVersionPacket
        )
    }
}