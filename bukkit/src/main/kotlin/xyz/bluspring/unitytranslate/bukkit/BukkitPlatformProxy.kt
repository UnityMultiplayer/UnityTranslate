package xyz.bluspring.unitytranslate.bukkit

import kotlinx.coroutines.Runnable
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import xyz.bluspring.modernnetworking.bukkit.api.BukkitNetworkSender
import xyz.bluspring.modernnetworking.bukkit.api.BukkitNetworkSender.sendPacketServer
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.PlatformProxy
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.*

class BukkitPlatformProxy : PlatformProxy(UnityTranslateBukkit.plugin.instance) {
    val plugin = UnityTranslateBukkit.plugin

    override val modVersion: String
        get() = plugin.pluginMeta.version

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        return plugin.server.getPlayer(uuid)?.hasPermission(permission) == true
    }

    override fun setClientPlayerLanguage(language: Language) {
        TODO("Not yet implemented")
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

    override fun canHearPlayer(player: UUID, other: UUID): Boolean {
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

    override fun handleTranscriptClient(
        uuid: UUID,
        language: Language,
        index: Int,
        updateTime: Long,
        toSend: Map<Language, String>
    ) {
        TODO("Not yet implemented")
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

    override fun sendPacketServer(player: UUID, packet: UTPacket) {
        val player = Bukkit.getPlayer(player) ?: return
        player.sendPacketServer(packet)
    }

    override fun broadcastPacketServer(packet: UTPacket) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendPacketServer(packet)
        }
    }

    override fun sendErrorMessage(player: UUID, message: String) {
        plugin.server.getPlayer(player)?.sendMessage("${ChatColor.RED}$message")
    }

    override fun sendPacketClient(packet: UTPacket) {
        TODO("Not yet implemented")
    }
}