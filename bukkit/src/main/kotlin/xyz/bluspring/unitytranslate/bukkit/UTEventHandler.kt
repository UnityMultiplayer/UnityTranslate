package xyz.bluspring.unitytranslate.bukkit

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import xyz.bluspring.modernnetworking.bukkit.api.BukkitNetworkSender

class UTEventHandler(val plugin: UnityTranslateBukkit) : Listener {
    @EventHandler
    fun onPlayerJoin(ev: PlayerJoinEvent) {
        plugin.instance.serverNetworking.modVersions.remove(ev.player.uniqueId)
        plugin.instance.serverNetworking.maximumProtocolVersions.remove(ev.player.uniqueId)


    }
}