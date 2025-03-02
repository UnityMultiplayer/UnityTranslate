package xyz.bluspring.unitytranslate.bukkit

import org.bukkit.Bukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.java.JavaPlugin
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.PacketIds.asPacketId
import xyz.bluspring.unitytranslate.common.util.Permissions

class UnityTranslateBukkit : JavaPlugin() {
    val instance = UnityTranslate(this.dataFolder.toPath())

    override fun onEnable() {
        plugin = this
        instance.proxy = BukkitPlatformProxy(this)
        instance.init()

        this.server.pluginManager.registerEvents(UTEventHandler(this), this)

        val packetHandler = UTPacketHandler(this)
        for (definition in PacketIds.definitions) {
            for (packet in definition.packets) {
                if (packet.direction != PacketBuilder.Direction.CLIENTBOUND) // serverbound
                    Bukkit.getMessenger().registerIncomingPluginChannel(this, packet.id.asPacketId(), packetHandler)

                if (packet.direction != PacketBuilder.Direction.SERVERBOUND) // clientbound
                    Bukkit.getMessenger().registerOutgoingPluginChannel(this, packet.id.asPacketId())
            }
        }

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