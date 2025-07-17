package xyz.bluspring.unitytranslate.common.network.v0

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.NetworkCodec
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.PacketDefinitions
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0ServerSupportPacket

object V0Packets : PacketDefinitions {
    private fun <T : NetworkPacket, B : ByteBuf> createDefinition(id: String, codec: NetworkCodec<T, B>): PacketDefinition<T, B> {
        return PacketDefinition(UnityTranslate.MOD_ID, id, codec)
    }

    val SERVER_SUPPORT = createDefinition("server_support", V0ServerSupportPacket.CODEC)

    override val clientboundPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>>
        get() = setOf()
    override val serverboundPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>>
        get() = setOf(SERVER_SUPPORT)
    override val dualPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>>
        get() = setOf()

}