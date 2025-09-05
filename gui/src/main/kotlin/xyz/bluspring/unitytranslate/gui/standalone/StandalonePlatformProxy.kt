package xyz.bluspring.unitytranslate.gui.standalone

import gg.essential.universal.UI18n
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.PlatformProxy
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.UUID

class StandalonePlatformProxy : PlatformProxy(UnityTranslate.instance) {
    override val modVersion: String
        get() = this::class.java.getResource("/version.txt")!!.readText()

    override val isActive: Boolean
        get() = try {
            Class.forName("xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC", false, StandalonePlatformProxy::class.java.classLoader)
        } catch (_: Throwable) { null } == null // this seems like a reasonable detection?

    override fun isLoaded(name: String): Boolean {
        return false
    }

    override fun isClient(): Boolean {
        return true
    }

    override fun serverSupportsTranslations(): Boolean {
        return false
    }

    override fun doesPlayerExist(uuid: UUID): Boolean {
        return false
    }

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        return true
    }

    override fun setClientPlayerLanguage(language: Language) {

    }

    override fun getTranslation(key: String, vararg args: String): String {
        return UI18n.i18n(key, *args)
    }

    override fun canHearPlayer(player: UUID, other: UUID): Boolean {
        return true
    }

    override fun getSqDistance(player: UUID, other: UUID): Double {
        return 0.0
    }

    override fun getAllPlayersInLevel(player: UUID): List<UUID> {
        return emptyList()
    }

    override fun sendPacketServer(
        player: UUID,
        packet: UTPacket
    ) {
        TODO("Not yet implemented")
    }

    override fun broadcastPacketServer(packet: UTPacket) {
        TODO("Not yet implemented")
    }

    override fun sendPacketClient(packet: UTPacket) {
        TODO("Not yet implemented")
    }

    override fun sendErrorMessage(player: UUID, message: String) {
        TODO("Not yet implemented")
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
}