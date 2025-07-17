package xyz.bluspring.unitytranslate.common.network

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1ClientboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1MarkIncompletePacket
import java.util.EnumSet
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

class UTServerNetworking(val instance: UnityTranslate) {
    val usedLanguages = ConcurrentHashMap<UUID, EnumSet<Language>>()
    val clientTranslators = ConcurrentLinkedDeque<UUID>()
    val maximumProtocolVersions = ConcurrentHashMap<UUID, Int>()
    val modVersions = ConcurrentHashMap<UUID, String>()

    fun getMaximumProtocolVersion(uuid: UUID): Int {
        return maximumProtocolVersions.computeIfAbsent(uuid) { 0 }
    }

    fun broadcastTranslations(uuid: UUID, sourceLanguage: Language, index: Int, updateTime: Long, translations: Map<Language, String?>, originalLine: String) {
        val players = if (instance.voiceChat != null)
            instance.voiceChat!!.getNearbyPlayers(uuid)
        else
            instance.proxy.getAllPlayersInLevel(uuid)

        for (player in players) {
            if (instance.voiceChat?.isPlayerDeafened(player) == true)
                continue

            instance.proxy.sendPacketServer(player, V1ClientboundSendTranscriptPacket(uuid, sourceLanguage, index, updateTime, translations.mapValues { it.value ?: originalLine }))

            translations.filter { it.value == null }.forEach { lang, _ ->
                instance.proxy.sendPacketServer(player, V1MarkIncompletePacket(uuid, sourceLanguage, lang, index, true))
            }
        }
    }
}