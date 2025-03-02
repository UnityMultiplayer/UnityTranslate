package xyz.bluspring.unitytranslate.bukkit

import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.GameMode
import xyz.bluspring.unitytranslate.bukkit.util.PacketWriter
import xyz.bluspring.unitytranslate.common.PlatformProxy
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketBuilder.DataType
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.PacketIds.asPacketId
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.*

class BukkitPlatformProxy(val plugin: UnityTranslateBukkit) : PlatformProxy(plugin.instance) {
    override val modVersion: String
        get() = plugin.pluginMeta.version

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        return plugin.server.getPlayer(uuid)?.hasPermission(permission) == true
    }

    override fun doesPlayerExist(uuid: UUID): Boolean {
        return plugin.server.getPlayer(uuid) != null
    }

    override fun getTranslation(key: String, vararg args: String): String {
        return key
    }

    override fun isClient(): Boolean {
        return false
    }

    override fun isLoaded(name: String): Boolean {
        return plugin.server.pluginManager.isPluginEnabled(name)
    }

    override fun serverSupportsTranslations(): Boolean {
        return true
    }

    override fun areBothSpectator(player: UUID, other: UUID): Boolean {
        val player = plugin.server.getPlayer(player) ?: return false
        val other = plugin.server.getPlayer(other) ?: return false

        // i know. this is a mess.
        if (player.gameMode == GameMode.SPECTATOR && other.gameMode == GameMode.SPECTATOR)
            return true
        else if (player.gameMode == GameMode.SPECTATOR && other.gameMode != GameMode.SPECTATOR)
            return true
        else if (player.gameMode != GameMode.SPECTATOR && other.gameMode == GameMode.SPECTATOR)
            return false

        return true
    }

    override fun queue(runnable: Runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable)
    }

    override fun getAllPlayersInLevel(player: UUID): List<UUID> {
        val playerLevel = plugin.server.getPlayer(player)?.world ?: return emptyList()

        return playerLevel.players.map { it.uniqueId }
    }

    override fun getSqDistance(player: UUID, other: UUID): Double {
        val player = plugin.server.getPlayer(player) ?: return 15000.0
        val other = plugin.server.getPlayer(other) ?: return 150000.0

        return player.location.distanceSquared(other.location)
    }

    fun <T : UTPacket> serializePacket(definition: PacketBuilder<T>, packet: T): PacketWriter {
        val writer = PacketWriter()

        for (typeDef in definition.types) {
            writeType(writer, typeDef, typeDef.getter.invoke(packet)!!)
        }

        return writer
    }

    override fun sendPacketServer(player: UUID, packet: UTPacket) {
        val definition = PacketIds.getPacketDefinition(packet)
        val writer = serializePacket(definition, packet)

        Bukkit.getPlayer(player)?.sendPluginMessage(plugin, definition.id.asPacketId(), writer.asByteArray())
    }

    override fun broadcastPacketServer(packet: UTPacket) {
        val definition = PacketIds.getPacketDefinition(packet)
        val writer = serializePacket(definition, packet)

        for (player in Bukkit.getOnlinePlayers()) {
            player.sendPluginMessage(plugin, definition.id.asPacketId(), writer.asByteArray())
        }
    }

    private fun writeType(writer: PacketWriter, typeDef: PacketBuilder.DataValue<*, *>, value: Any) {
        when (typeDef.type) {
            DataType.MAP -> {
                if (value !is Map<*, *>)
                    throw IllegalStateException()

                if (typeDef !is PacketBuilder.MapDataValue<*, *, *>)
                    throw IllegalStateException()

                writer.writeVarInt(value.size)
                for ((key, value) in value) {
                    writeType(writer, PacketBuilder.SingleDataValue<Any, Any>(typeDef.keyType) { key!! }, key!!)
                    writeType(writer, PacketBuilder.SingleDataValue<Any, Any>(typeDef.valueType) { value!! }, value!!)
                }
            }

            DataType.ENUM -> writer.writeEnum(value as Enum<*>)
            DataType.STRING -> writer.writeString(value as String)
            DataType.VAR_LONG -> writer.writeVarLong(value as Long)
            DataType.BOOLEAN -> writer.writeBoolean(value as Boolean)
            DataType.ENUM_SET -> writer.writeEnumSetUnchecked(value as EnumSet<*>, (typeDef as PacketBuilder.SingleDataValue<*, Enum<*>>).additionalType!! as Class<out Enum<*>>)
            DataType.VAR_INT -> writer.writeVarInt(value as Int)
            DataType.LIST -> {
                writer.writeVarInt((value as List<*>).size)

                for (item in value) {
                    writeType(writer, PacketBuilder.SingleDataValue<Any, Any>((typeDef as PacketBuilder.ListDataValue<Any, Any>).valueType) { item!! }, item!!)
                }
            }
            DataType.UUID -> writer.writeUUID(value as UUID)
            DataType.BYTE -> writer.writeByte(value as Byte)
        }
    }
}