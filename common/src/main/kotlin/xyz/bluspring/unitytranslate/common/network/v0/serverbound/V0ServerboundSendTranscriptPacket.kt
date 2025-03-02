package xyz.bluspring.unitytranslate.common.network.v0.serverbound

import kotlinx.coroutines.launch
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.util.Permissions
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

data class V0ServerboundSendTranscriptPacket(val sourceLanguage: Language, val text: String, val index: Int, val updateTime: Long) : UTPacket {
    override fun handleServer(player: UUID) {
        if (!instance.proxy.hasPermission(player, Permissions.REQUEST_TRANSLATIONS))
            return

        val translationsToSend = ConcurrentHashMap<Language, String?>()
        val usedLanguages = instance.serverNetworking.usedLanguages.values.flatten()
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