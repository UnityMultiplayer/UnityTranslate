package xyz.bluspring.unitytranslate.common.compat.voicechat

import de.maxhenkel.voicechat.api.*
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent
import xyz.bluspring.unitytranslate.common.UnityTranslate
import java.util.*

@ForgeVoicechatPlugin
class SimpleVoiceChatCompat : VoicechatPlugin {
    override fun registerEvents(registration: EventRegistration) {
        super.registerEvents(registration)

        registration.registerEvent(VoicechatServerStartedEvent::class.java) {
            voiceChatServer = it.voicechat
        }

        registration.registerEvent(ClientVoicechatInitializationEvent::class.java) {
            voiceChatClient = it.voicechat
        }
    }

    override fun getPluginId(): String {
        return UnityTranslate.MOD_ID
    }

    companion object : UTVoiceChatCompat {
        lateinit var voiceChatServer: VoicechatServerApi
        lateinit var voiceChatClient: VoicechatClientApi

        override val maxVoiceDistance: Double
            get() {
                return voiceChatServer.voiceChatDistance
            }

        override fun isPlayerDeafened(player: UUID): Boolean {
            val connection = voiceChatServer.getConnectionOf(player) ?: return true
            return connection.isDisabled
        }

        override fun isPlayerMutedOrDeafened(player: UUID): Boolean {
            return isPlayerDeafened(player)
        }

        override fun isPlayerAudible(player: UUID): Boolean {
            // FIXME: SVC doesn't provide an easy way of detecting this...
            return true
        }

        override fun playerSharesGroup(player: UUID, other: UUID): Boolean {
            val firstGroup = voiceChatServer.getConnectionOf(player)?.group ?: return false
            if (firstGroup.type == Group.Type.OPEN)
                return true

            val secondGroup = voiceChatServer.getConnectionOf(other)?.group ?: return false
            if (secondGroup.type == Group.Type.ISOLATED && firstGroup.id != secondGroup.id)
                return false

            return firstGroup.id == secondGroup.id
        }
    }
}