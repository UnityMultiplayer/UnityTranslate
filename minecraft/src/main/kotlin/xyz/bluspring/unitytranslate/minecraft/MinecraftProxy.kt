package xyz.bluspring.unitytranslate.minecraft

import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
//? if <= 1.19.2 {
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
//?}
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import xyz.bluspring.unitytranslate.common.Language
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
        //? if >= 1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(UnityTranslate.MOD_ID, name)
        *///?} else {
        return ResourceLocation(UnityTranslate.MOD_ID, name)
        //?}
    }

    fun getConfigPath(): Path {
        throw IllegalStateException("Not abstracted!")
    }

    fun literal(text: String): MutableComponent {
        //? if <= 1.18.2 {
        return TextComponent(text)
        //?} else {
        /*return Component.literal(text)
        *///?}
    }

    fun translatable(key: String, vararg args: Any): MutableComponent {
        //? if <= 1.18.2 {
        return TranslatableComponent(key, *args)
        //?} else {
        /*return Component.translatable(key, *args)
        *///?}
    }

    fun String.asPacketResource(): ResourceLocation {
        return ResourceLocation.tryParse(this.asPacketId())!!
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

    fun getPlayer(uuid: UUID): Player? {
        return server.playerList.getPlayer(uuid)
    }

    fun getEntityLevel(entity: Entity): Level? {
        return entity.level/*? if >= 1.20.1 {*//*()*//*?}*/
    }

    val Language.text: Component
        get() = MinecraftProxy.translatable(this.translationKey)
}