package xyz.bluspring.unitytranslate.common.network.v1.dual

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets
import java.util.UUID

data class V1SetClientTranslationPacket(val enabled: Boolean) : UTPacket {
    override fun handleClient() {
        throw IllegalStateException("No client implementation has been implemented! This is supposed to be modified via mixin!")
    }

    override fun handleServer(player: UUID) {
        val clientTranslators = instance.serverNetworking.clientTranslators

        if (!clientTranslators.contains(player) && enabled)
            clientTranslators.add(player)
        else if (clientTranslators.contains(player) && !enabled)
            clientTranslators.remove(player)
    }

    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.SET_CLIENT_TRANSLATION

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.BOOL, V1SetClientTranslationPacket::enabled,
            ::V1SetClientTranslationPacket
        )
    }
}