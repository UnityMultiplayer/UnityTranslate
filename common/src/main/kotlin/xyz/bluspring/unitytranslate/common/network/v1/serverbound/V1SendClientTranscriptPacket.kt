package xyz.bluspring.unitytranslate.common.network.v1.serverbound

import kotlinx.coroutines.launch
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.util.Permissions
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class V1SendClientTranscriptPacket(
    val sourceLanguage: Language,
    val index: Int,
    val updateTime: Long,
    val translated: Map<Language, String>
) : UTPacket {
    override fun handleServer(player: UUID) {
        if (!instance.proxy.hasPermission(player, Permissions.REQUEST_TRANSLATIONS))
            return

        instance.proxy.queue {
            instance.serverNetworking.broadcastTranslations(player, sourceLanguage, index, updateTime, translated, "<???>")
        }

        val usedLanguages = instance.serverNetworking.usedLanguages.values.flatten().filter { !translated.keys.contains(it) }

        if (usedLanguages.isEmpty() || translated.isEmpty())
            return

        val translationsToSend = ConcurrentHashMap<Language, String?>()
        val sourceLanguage = translated.keys.first()
        val text = translated[sourceLanguage]!!

        for (language in usedLanguages) {
            instance.translatorManager.scope.launch {
                val translated = instance.translatorManager.queueTranslation(text, sourceLanguage, language, player, index)

                translationsToSend[language] = translated

                instance.proxy.queue {
                    if (translationsToSend.isEmpty())
                        return@queue

                    instance.serverNetworking.broadcastTranslations(player, sourceLanguage, index, updateTime, translationsToSend, text)
                    translationsToSend.clear()
                }
            }
        }
    }
}