package xyz.bluspring.unitytranslate.common.network.v0.serverbound

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v0.V0Packets
import java.util.UUID

object V0ServerSupportPacket : UTPacket {
    val CODEC = NetworkCodecs.unit(V0ServerSupportPacket)

    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V0Packets.SERVER_SUPPORT

    override fun handleServer(player: UUID) {
        super.handleServer(player)

        UnityTranslate.instance.proxy.sendErrorMessage(player, "Your client was detected to be using UnityTranslate v0.1, but the server is using v${UnityTranslate.instance.proxy.modVersion}!")
    }
}