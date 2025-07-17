package xyz.bluspring.unitytranslate.minecraft

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import xyz.bluspring.modernnetworking.api.minecraft.VanillaPacketSender
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.PlatformProxy
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import java.util.*

abstract class MinecraftPlatformProxy : PlatformProxy(UnityTranslate.instance) {
    override fun serverSupportsTranslations(): Boolean {
        return if (isClient())
            UnityTranslateMCClient.serverHasTranslations
        else
            true
    }

    override fun sendPacketServer(player: UUID, packet: UTPacket) {
        VanillaPacketSender.sendToPlayer(MinecraftProxy.getPlayer(player) as? ServerPlayer ?: return, packet)
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
        VanillaPacketSender.sendToServer(packet)
    }

    override fun doesPlayerExist(uuid: UUID): Boolean {
        return MinecraftProxy.getPlayer(uuid) != null
    }

    override fun getTranslation(key: String, vararg args: String): String {
        return MinecraftProxy.translatable(key, *args).string
    }

    override fun canHearPlayer(player: UUID, other: UUID): Boolean {
        val first = MinecraftProxy.getPlayer(player) ?: return false
        val second = MinecraftProxy.getPlayer(player) ?: return false

        return (first.isSpectator == second.isSpectator) || (first.isSpectator && !second.isSpectator)
    }

    override fun getSqDistance(player: UUID, other: UUID): Double {
        val first = MinecraftProxy.getPlayer(player) ?: return 32767.0
        val second = MinecraftProxy.getPlayer(player) ?: return 32767.0

        return first.distanceToSqr(second)
    }

    override fun getAllPlayersInLevel(player: UUID): List<UUID> {
        return MinecraftProxy.getPlayer(player)?.level/*? if >= 1.20.1 {*//*()*//*?} */
            ?.players()?.map { it.uuid } ?: emptyList()
    }

    override fun sendErrorMessage(player: UUID, message: String) {
        MinecraftProxy.getPlayer(player)?.displayClientMessage(MinecraftProxy.literal(message).withStyle(ChatFormatting.RED), false)
    }
}