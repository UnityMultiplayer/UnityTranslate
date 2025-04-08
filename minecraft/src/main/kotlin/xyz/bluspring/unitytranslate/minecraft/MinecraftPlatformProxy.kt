package xyz.bluspring.unitytranslate.minecraft

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.PlatformProxy
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketIds
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy.asPacketResource
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import java.util.*

class MinecraftPlatformProxy : PlatformProxy(UnityTranslateMC.instance) {
    override fun serverSupportsTranslations(): Boolean {
        return true
    }

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        //#if FABRIC
        val player = MinecraftProxy.getPlayer(uuid) ?: return false
        return Permissions.check(player, permission, true)
        //#endif
    }

    override fun sendPacketServer(player: UUID, packet: UTPacket) {
        //#if MC <= 1.20.4
        val definition = PacketIds.getPacketDefinition(packet, PacketBuilder.Direction.CLIENTBOUND)
        val buf = MinecraftProxy.buildPacket(definition, packet)

        ServerPlayNetworking.send(MinecraftProxy.server.playerList.getPlayer(player), definition.id.asPacketResource(), buf)
        //#else

        //#endif
    }

    override fun setClientPlayerLanguage(language: Language) {
        UnityTranslateMCClient.clientConfig.spokenLanguage = language
    }

    override fun handleTranscriptClient(
        uuid: UUID,
        sourceLanguage: Language,
        index: Int,
        updateTime: Long,
        toSend: Map<Language, String>
    ) {
        for ((language, text) in toSend) {
            if (language == sourceLanguage && MinecraftProxy.getPlayer(uuid) == Minecraft.getInstance().player)
                continue

            UnityTranslateMCClient.transcriptHolders[language]?.updateTranscript(MinecraftProxy.getPlayer(uuid) ?: continue, text, sourceLanguage, index, updateTime, false)
        }
    }

    override fun broadcastPacketServer(packet: UTPacket) {
        for (player in MinecraftProxy.server.playerList.players) {
            sendPacketServer(player.uuid, packet)
        }
    }

    override fun sendPacketClient(packet: UTPacket) {
        //#if MC <= 1.20.4
        val definition = PacketIds.getPacketDefinition(packet, PacketBuilder.Direction.SERVERBOUND)
        val buf = MinecraftProxy.buildPacket(definition, packet)
        ClientPlayNetworking.send(definition.id.asPacketResource(), buf)
        //#else

        //#endif
    }
}