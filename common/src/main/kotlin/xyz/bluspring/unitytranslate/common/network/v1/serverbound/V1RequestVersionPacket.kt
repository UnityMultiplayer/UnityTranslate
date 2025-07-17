package xyz.bluspring.unitytranslate.common.network.v1.serverbound

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1ResponseVersionPacket
import java.util.UUID

data class V1RequestVersionPacket(
    val protocolVersion: Int,
    val modVersion: String
) : UTPacket {
    override fun handleServer(player: UUID) {
        instance.serverNetworking.maximumProtocolVersions[player] = protocolVersion
        instance.serverNetworking.modVersions[player] = modVersion

        UnityTranslate.instance.proxy.sendPacketServer(player, V1ResponseVersionPacket(UnityTranslate.instance.proxy.modVersion))
    }

    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.REQUEST_VERSION

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.VAR_INT, V1RequestVersionPacket::protocolVersion,
            NetworkCodecs.STRING_UTF8, V1RequestVersionPacket::modVersion,
            ::V1RequestVersionPacket
        )
    }
}