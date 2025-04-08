package xyz.bluspring.unitytranslate.fabric.network

//#if FABRIC
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy.asPacketResource

object UTClientNetworkHandler {
    fun init() {
        for (definitions in PacketIds.definitions) {
            for (packetDef in definitions.packets) {
                if (packetDef.direction == PacketBuilder.Direction.SERVERBOUND)
                    continue

                ClientPlayNetworking.registerGlobalReceiver(packetDef.id.asPacketResource()) { client, handler, buf, sender ->
                    val values = mutableListOf<Any>()
                    for (dataDef in packetDef.types) {
                        values.add(MinecraftProxy.readType(buf, dataDef))
                    }

                    val packet = packetDef.build(*values.toTypedArray())
                    packet.handleClient()
                }
            }
        }
    }
}
//#endif