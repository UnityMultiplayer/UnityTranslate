package xyz.bluspring.unitytranslate

import dev.architectury.networking.NetworkManager
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
//? if >= 1.20.6 {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload
*///? }
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

//? if forge {
/*import net.minecraftforge.server.permission.events.PermissionGatherEvent
*///? } else if neoforge {
/*import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
*///? }

import java.nio.file.Path

interface PlatformProxy {
    fun isModLoaded(id: String): Boolean
    fun isClient(): Boolean

    val modVersion: String
    val configDir: Path
    val gameDir: Path
    val isDev: Boolean

    fun createByteBuf(): FriendlyByteBuf {
        return FriendlyByteBuf(Unpooled.buffer())
    }

    //? if >= 1.20.6 {
     /*fun sendPacketClient(payload: CustomPacketPayload) {
        NetworkManager.sendToServer(payload)
    *///? } else {
    fun sendPacketClient(id: ResourceLocation, buf: FriendlyByteBuf) {
        NetworkManager.sendToServer(id, buf)
    //? }
    }

    //? if >= 1.20.6 {
    /*fun sendPacketServer(player: ServerPlayer, payload: CustomPacketPayload) {
        NetworkManager.sendToPlayer(player, payload)
    *///? } else {
    fun sendPacketServer(player: ServerPlayer, id: ResourceLocation, buf: FriendlyByteBuf) {
        NetworkManager.sendToPlayer(player, id, buf)
    //? }
    }

    //? if forge_like {
    /*fun registerPermissions(event: PermissionGatherEvent.Nodes)
    *///? }

    fun hasTranscriptPermission(player: Player): Boolean
}