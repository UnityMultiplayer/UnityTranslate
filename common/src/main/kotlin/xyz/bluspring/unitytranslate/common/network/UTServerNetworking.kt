package xyz.bluspring.unitytranslate.common.network

import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.apache.commons.codec.language.bm.Lang
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.config.UnityTranslateConfig
import xyz.bluspring.unitytranslate.common.network.v0.clientbound.V0ClientboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v0.clientbound.V0MarkIncompletePacket
import xyz.bluspring.unitytranslate.common.util.Permissions
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
        if (instance.voiceChat != null) {
            val nearby = instance.voiceChat!!.getNearbyPlayers(uuid)

            for (player in nearby) {
                if (instance.voiceChat!!.isPlayerDeafened(player) && player != uuid)
                    continue

                instance.proxy.sendPacketServer(player, V0ClientboundSendTranscriptPacket(uuid, sourceLanguage, index, updateTime, translations.mapValues { it.value ?: originalLine }))

                translations.filter { it.value == null }.forEach { lang, _ ->
                    instance.proxy.sendPacketServer(player, V0MarkIncompletePacket(sourceLanguage, lang, uuid, index, true))
                }
            }
        } else {
            for (player in instance.proxy.getAllPlayersInLevel(uuid)) {
                instance.proxy.sendPacketServer(player, V0ClientboundSendTranscriptPacket(uuid, sourceLanguage, index, updateTime, translations.mapValues { it.value ?: originalLine }))

                translations.filter { it.value == null }.forEach { lang, _ ->
                    instance.proxy.sendPacketServer(player, V0MarkIncompletePacket(sourceLanguage, lang, uuid, index, true))
                }
            }
        }
    }
}