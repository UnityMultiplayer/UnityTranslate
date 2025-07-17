package xyz.bluspring.unitytranslate.common.network

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.AbstractNetworkRegistry
import xyz.bluspring.modernnetworking.api.PacketDefinition

interface PacketDefinitions {
    val clientboundPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>>
    val serverboundPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>>
    val dualPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>>

    fun <C, S> register(registry: AbstractNetworkRegistry<C, S>) {
        for (definition in clientboundPackets) {
            registry.registerClientbound(definition)
        }

        for (definition in serverboundPackets) {
            registry.registerServerbound(definition)
        }

        for (definition in dualPackets) {
            registry.registerDual(definition)
        }
    }
}