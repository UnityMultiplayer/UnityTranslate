package xyz.bluspring.unitytranslate.bukkit

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import xyz.bluspring.modernnetworking.bukkit.api.RegisterNetworkRegistryEvent
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.PacketIds.asPacketId
import xyz.bluspring.unitytranslate.common.util.Permissions

class UnityTranslateBukkit : JavaPlugin() {
    val instance = UnityTranslate(this.dataFolder.toPath())

    @EventHandler
    fun onRegisterNetwork(ev: RegisterNetworkRegistryEvent) {
        val registry = ev.create(this, "unitytranslate")

        for (definitions in PacketIds.definitions) {
            definitions.register(registry)

            for (definition in definitions.serverboundPackets) {
                registry.addServerboundHandler(definition) { packet, ctx -> packet.handleServer(ctx.player.uniqueId) }
            }

            for (definition in definitions.clientboundPackets) {
                Bukkit.getMessenger().registerOutgoingPluginChannel(this, definition.id.asPacketId())
            }
        }
    }

    override fun onEnable() {
        plugin = this
        instance.init()

        this.server.pluginManager.registerEvents(UTEventHandler(this), this)

        for (permName in Permissions.permissions) {
            try {
                server.pluginManager.addPermission(Permission(permName, PermissionDefault.OP))
            } catch (_: Throwable) {}
        }
    }

    companion object {
        lateinit var plugin: UnityTranslateBukkit
    }
}