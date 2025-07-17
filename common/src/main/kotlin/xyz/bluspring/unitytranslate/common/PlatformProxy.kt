package xyz.bluspring.unitytranslate.common

import kotlinx.coroutines.Runnable
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class PlatformProxy(val instance: UnityTranslate) {
    val playerLanguage = ConcurrentHashMap<UUID, Language>()

    abstract val modVersion: String
    abstract fun isLoaded(name: String): Boolean
    abstract fun isClient(): Boolean
    abstract fun serverSupportsTranslations(): Boolean
    abstract fun doesPlayerExist(uuid: UUID): Boolean
    abstract fun hasPermission(uuid: UUID, permission: String): Boolean
    abstract fun setClientPlayerLanguage(language: Language)

    /**
     * Sets the language of the provided player. If the UUID is null, it refers to the client's player.
     */
    open fun setPlayerLanguage(uuid: UUID, language: Language) {
        playerLanguage[uuid] = language
    }

    abstract fun getTranslation(key: String, vararg args: String): String
    abstract fun canHearPlayer(player: UUID, other: UUID): Boolean
    abstract fun getSqDistance(player: UUID, other: UUID): Double
    abstract fun getAllPlayersInLevel(player: UUID): List<UUID>
    abstract fun sendPacketServer(player: UUID, packet: UTPacket)
    abstract fun broadcastPacketServer(packet: UTPacket)
    abstract fun sendPacketClient(packet: UTPacket)

    abstract fun sendErrorMessage(player: UUID, message: String)

    open fun queue(runnable: Runnable) {
        runnable.run()
    }

    abstract fun handleTranscriptClient(uuid: UUID, language: Language, index: Int, updateTime: Long, toSend: Map<Language, String>)
}