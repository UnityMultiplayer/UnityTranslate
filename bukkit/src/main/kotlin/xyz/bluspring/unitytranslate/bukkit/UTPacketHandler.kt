package xyz.bluspring.unitytranslate.bukkit

import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import xyz.bluspring.unitytranslate.bukkit.util.PacketReader
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketBuilder.DataType
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.PacketIds.asPacketId

class UTPacketHandler(val plugin: UnityTranslateBukkit) : PluginMessageListener {
    private fun readType(reader: PacketReader, dataDef: PacketBuilder.DataValue<*, *>): Any {
        return when (dataDef.type) {
            DataType.VAR_INT -> reader.readVarInt()
            DataType.ENUM -> reader.readEnumUnchecked((dataDef as PacketBuilder.SingleDataValue).additionalType!!)
            DataType.MAP -> {
                if (dataDef !is PacketBuilder.MapDataValue<*, *, *>)
                    throw IllegalStateException("how?")

                val length = reader.readVarInt()
                val map = mutableMapOf<Any, Any>()

                for (i in 0 until length) {
                    val key = readType(reader, PacketBuilder.SingleDataValue<Any, Any>(dataDef.keyType) { throw IllegalStateException("none") }.apply {
                        this.additionalType = dataDef.additionalKeyType
                    })

                    val value = readType(reader, PacketBuilder.SingleDataValue<Any, Any>(dataDef.valueType) { throw IllegalStateException("none") }.apply {
                        this.additionalType = dataDef.additionalValueType
                    })

                    map[key] = value
                }

                map
            }
            DataType.BYTE -> reader.readByte()
            DataType.LIST -> {
                if (dataDef !is PacketBuilder.ListDataValue<*, *>)
                    throw IllegalStateException("how?")

                val length = reader.readVarInt()
                val list = mutableListOf<Any>()

                for (i in 0 until length) {
                    list.add(readType(reader, PacketBuilder.SingleDataValue<Any, Any>(dataDef.valueType) { throw IllegalStateException("none") }))
                }

                list
            }
            DataType.STRING -> reader.readString()
            DataType.UUID -> reader.readUUID()
            DataType.BOOLEAN -> reader.readBoolean()
            DataType.ENUM_SET -> reader.readEnumSet((dataDef as PacketBuilder.SingleDataValue<*, *>).additionalType!! as Class<out Enum<*>>)
            DataType.VAR_LONG -> reader.readVarLong()
        }
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        val reader = PacketReader(message)

        for (definitions in PacketIds.definitions) {
            for (packetDef in definitions.packets) {
                if (packetDef.id.asPacketId() == channel) {
                    val values = mutableListOf<Any>()
                    for (dataDef in packetDef.types) {
                        values.add(readType(reader, dataDef))
                    }

                    val packet = packetDef.build(*values.toTypedArray())
                    packet.handleServer(player.uniqueId)

                    return
                }
            }
        }
    }
}