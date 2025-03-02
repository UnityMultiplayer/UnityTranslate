package xyz.bluspring.unitytranslate.minecraft

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketBuilder.DataType
import xyz.bluspring.unitytranslate.common.network.PacketIds.asPacketId
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.nio.file.Path
import java.util.*

object MinecraftProxy {
    lateinit var server: MinecraftServer

    fun id(name: String): ResourceLocation {
        return ResourceLocation(UnityTranslate.MOD_ID, name)
    }

    fun getConfigPath(): Path {
        throw IllegalStateException("Not abstracted!")
    }

    fun literal(text: String): MutableComponent {
        return TextComponent(text)
    }

    fun translatable(key: String): MutableComponent {
        return TranslatableComponent(key)
    }

    fun String.asPacketResource(): ResourceLocation {
        return ResourceLocation.tryParse(this.asPacketId())!!
    }

    fun <T : UTPacket> buildPacket(definition: PacketBuilder<T>, packet: T): FriendlyByteBuf {
        val buf = FriendlyByteBuf(Unpooled.buffer())

        return buildPacket(buf, definition, packet)
    }

    fun <T : UTPacket> buildPacket(buf: FriendlyByteBuf, definition: PacketBuilder<T>, packet: T): FriendlyByteBuf {
        for (typeDef in definition.types) {
            writeType(buf, typeDef, typeDef.getter.invoke(packet)!!)
        }

        return buf
    }

    private fun <E : Enum<E>> writeEnumSetUnchecked(writer: FriendlyByteBuf, enumSet: EnumSet<*>, enumClass: Class<E>) {
        val enums = enumClass.enumConstants
        val bitSet = BitSet(enums.size)
        for (i in 0 until enums.size) {
            bitSet.set(i, enumSet.contains(enums[i]))
        }

        val length = -Math.floorDiv(-enums.size, 8)
        writer.writeBytes(bitSet.toByteArray().copyOf(length))
    }

    private fun writeType(writer: FriendlyByteBuf, typeDef: PacketBuilder.DataValue<*, *>, value: Any) {
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
            DataType.STRING -> writer.writeUtf(value as String)
            DataType.VAR_LONG -> writer.writeVarLong(value as Long)
            DataType.BOOLEAN -> writer.writeBoolean(value as Boolean)
            DataType.ENUM_SET -> writeEnumSetUnchecked(writer, value as EnumSet<*>, (typeDef as PacketBuilder.SingleDataValue<*, Enum<*>>).additionalType!! as Class<out Enum<*>>)
            DataType.VAR_INT -> writer.writeVarInt(value as Int)
            DataType.LIST -> {
                writer.writeVarInt((value as List<*>).size)

                for (item in value) {
                    writeType(writer, PacketBuilder.SingleDataValue<Any, Any>((typeDef as PacketBuilder.ListDataValue<Any, Any>).valueType) { item!! }, item!!)
                }
            }
            DataType.UUID -> writer.writeUUID(value as UUID)
            DataType.BYTE -> writer.writeByte(value as Int)
        }
    }

    fun getPlayer(uuid: UUID): Player? {
        return server.playerList.getPlayer(uuid)
    }

    fun getEntityLevel(entity: Entity): Level? {
        return entity.level
    }
}