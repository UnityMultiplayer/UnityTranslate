package xyz.bluspring.unitytranslate.fabric.network

//#if FABRIC
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.FriendlyByteBuf
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketBuilder.DataType
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy.asPacketResource
import java.util.*

object UTServerNetworkHandler {
    fun <E : Enum<E>> FriendlyByteBuf.readEnumSet(enumClass: Class<E>): EnumSet<E> {
        val enums = enumClass.enumConstants
        val bitSet = BitSet.valueOf(this.readByteArray(-Math.floorDiv(-enums.size, 8)))
        val enumSet = EnumSet.noneOf(enumClass)

        for (i in 0 until enums.size) {
            if (bitSet.get(i)) {
                enumSet.add(enums[i])
            }
        }

        return enumSet
    }

    fun readType(reader: FriendlyByteBuf, dataDef: PacketBuilder.DataValue<*, *>): Any {
        return when (dataDef.type) {
            DataType.VAR_INT -> reader.readVarInt()
            DataType.ENUM -> reader.readEnum((dataDef as PacketBuilder.SingleDataValue).additionalType!! as Class<out Enum<*>>)
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
            DataType.STRING -> reader.readUtf()
            DataType.UUID -> reader.readUUID()
            DataType.BOOLEAN -> reader.readBoolean()
            DataType.ENUM_SET -> reader.readEnumSet((dataDef as PacketBuilder.SingleDataValue<*, *>).additionalType!! as Class<out Enum<*>>)
            DataType.VAR_LONG -> reader.readVarLong()
        }
    }

    fun init() {
        for (definitions in PacketIds.definitions) {
            for (packetDef in definitions.packets) {
                ServerPlayNetworking.registerGlobalReceiver(packetDef.id.asPacketResource()) { server, player, handler, buf, sender ->
                    val values = mutableListOf<Any>()
                    for (dataDef in packetDef.types) {
                        values.add(readType(buf, dataDef))
                    }

                    val packet = packetDef.build(*values.toTypedArray())
                    packet.handleServer(player.uuid)
                }
            }
        }
    }
}
//#endif