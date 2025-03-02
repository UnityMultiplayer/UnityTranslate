package xyz.bluspring.unitytranslate.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import xyz.bluspring.unitytranslate.common.network.v0.clientbound.V0ServerSupportPacket

class UTEventHandler(val plugin: UnityTranslateBukkit) : Listener {
    @EventHandler
    fun onPlayerJoin(ev: PlayerJoinEvent) {
        plugin.instance.serverNetworking.modVersions.remove(ev.player.uniqueId)
        plugin.instance.serverNetworking.maximumProtocolVersions.remove(ev.player.uniqueId)

        plugin.instance.proxy.sendPacketServer(ev.player.uniqueId, V0ServerSupportPacket())
    }
}