package xyz.bluspring.unitytranslate.network.payloads

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.network.PacketDefinitions

object ServerSupportPayload : NetworkPacket {
    val CODEC = NetworkCodecs.unit(ServerSupportPayload)

    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = PacketDefinitions.SERVER_SUPPORT
}
