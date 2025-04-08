package xyz.bluspring.unitytranslate.common

import kotlinx.coroutines.Runnable
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class PlatformProxy(val instance: UnityTranslate) {
    val playerLanguage = ConcurrentHashMap<UUID, Language>()

    open val modVersion: String
        get() {
            throw IllegalStateException("Not abstracted!")
        }

    open fun isLoaded(name: String): Boolean {
        throw IllegalStateException("Not abstracted!")
    }

    open fun isClient(): Boolean {
        throw IllegalStateException("Not abstracted!")
    }

    open fun serverSupportsTranslations(): Boolean {
        throw IllegalStateException("Not abstracted!")
    }

    open fun doesPlayerExist(uuid: UUID): Boolean {
        throw IllegalStateException("Not abstracted!")
    }

    open fun hasPermission(uuid: UUID, permission: String): Boolean {
        throw IllegalStateException("Not abstracted!")
    }

    open fun setClientPlayerLanguage(language: Language) {
        throw IllegalStateException("Not abstracted!")
    }

    /**
     * Sets the language of the provided player. If the UUID is null, it refers to the client's player.
     */
    open fun setPlayerLanguage(uuid: UUID, language: Language) {
        playerLanguage[uuid] = language
    }

    open fun getTranslation(key: String, vararg args: String): String {
        throw IllegalStateException("Not abstracted!")
    }

    open fun areBothSpectator(player: UUID, other: UUID): Boolean {
        throw IllegalStateException("Not abstracted!")
    }

    open fun getSqDistance(player: UUID, other: UUID): Double {
        throw IllegalStateException("Not abstracted!")
    }

    open fun getAllPlayersInLevel(player: UUID): List<UUID> {
        throw IllegalStateException("Not abstracted!")
    }

    open fun sendPacketServer(player: UUID, packet: UTPacket) {
        throw IllegalStateException("Not abstracted!")
    }

    open fun broadcastPacketServer(packet: UTPacket) {
        throw IllegalStateException("Not abstracted!")
    }

    open fun sendPacketClient(packet: UTPacket) {
        throw IllegalStateException("Not abstracted!")
    }

    open fun queue(runnable: Runnable) {
        runnable.run()
    }

    open fun handleTranscriptClient(uuid: UUID, language: Language, index: Int, updateTime: Long, toSend: Map<Language, String>) {
        throw IllegalStateException("Not abstracted!")
    }
}