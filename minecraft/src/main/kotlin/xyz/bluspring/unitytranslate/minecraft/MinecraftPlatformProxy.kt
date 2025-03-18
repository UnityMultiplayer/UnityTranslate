package xyz.bluspring.unitytranslate.minecraft

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import xyz.bluspring.unitytranslate.common.PlatformProxy
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy.asPacketResource
import java.util.*

class MinecraftPlatformProxy : PlatformProxy(UnityTranslateMC.instance) {
    override fun serverSupportsTranslations(): Boolean {
        return true
    }

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        //#if FABRIC
        val player = MinecraftProxy.getPlayer(uuid) ?: return false
        return Permissions.check(player, permission)
        //#endif
    }

    override fun sendPacketServer(player: UUID, packet: UTPacket) {
        //#if MC <= 1.20.4
        val definition = PacketIds.getPacketDefinition(packet)
        val buf = MinecraftProxy.buildPacket(definition, packet)

        ServerPlayNetworking.send(MinecraftProxy.server.playerList.getPlayer(player), definition.id.asPacketResource(), buf)
        //#else

        //#endif
    }

    override fun broadcastPacketServer(packet: UTPacket) {
        for (player in MinecraftProxy.server.playerList.players) {
            sendPacketServer(player.uuid, packet)
        }
    }

    override fun sendPacketClient(packet: UTPacket) {
        //#if MC <= 1.20.4
        val definition = PacketIds.getPacketDefinition(packet)
        val buf = MinecraftProxy.buildPacket(definition, packet)
        ClientPlayNetworking.send(definition.id.asPacketResource(), buf)
        //#else

        //#endif
    }
}