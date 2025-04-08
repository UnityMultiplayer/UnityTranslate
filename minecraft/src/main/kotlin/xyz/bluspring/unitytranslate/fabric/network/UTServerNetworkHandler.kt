package xyz.bluspring.unitytranslate.fabric.network

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy.asPacketResource

object UTServerNetworkHandler {
    fun init() {
        for (definitions in PacketIds.definitions) {
            for (packetDef in definitions.packets) {
                if (packetDef.direction == PacketBuilder.Direction.CLIENTBOUND)
                    continue

                ServerPlayNetworking.registerGlobalReceiver(packetDef.id.asPacketResource()) { server, player, handler, buf, sender ->
                    val values = mutableListOf<Any>()
                    for (dataDef in packetDef.types) {
                        values.add(MinecraftProxy.readType(buf, dataDef))
                    }

                    val packet = packetDef.build(*values.toTypedArray())
                    packet.handleServer(player.uuid)
                }
            }
        }
    }
}
//#endif