package xyz.bluspring.unitytranslate.minecraft.mc12006

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.UTPacket

object UTModernNetworkHelper {
    val definitionToCodecMap = mutableMapOf<PacketBuilder<*>, StreamCodec<RegistryFriendlyByteBuf, *>>()
    val definitionToTypeMap = mutableMapOf<PacketBuilder<*>, CustomPacketPayload.Type<*>>()

    fun init() {
        for (definitions in PacketIds.definitions) {
            for (packetDef in definitions.packets) {
                definitionToCodecMap[packetDef] = object : StreamCodec<RegistryFriendlyByteBuf, UTPacket> {
                    override fun decode(buf: RegistryFriendlyByteBuf): UTPacket {
                        val values = mutableListOf<Any>()
                        for (typeDef in packetDef.types) {
                            values.add(UTServerNetworkHandler.readType(buf, typeDef))
                        }

                        return packetDef.build(*values.toTypedArray())
                    }

                    override fun encode(buf: RegistryFriendlyByteBuf, packet: UTPacket) {
                        MinecraftProxy.buildPacket<UTPacket>(buf, packetDef as PacketBuilder<UTPacket>, packet)
                    }
                }
            }
        }
    }
}