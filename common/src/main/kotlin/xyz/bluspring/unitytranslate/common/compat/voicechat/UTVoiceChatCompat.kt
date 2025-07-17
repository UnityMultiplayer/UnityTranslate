package xyz.bluspring.unitytranslate.common.compat.voicechat

import xyz.bluspring.unitytranslate.common.UnityTranslate
import java.util.UUID

interface UTVoiceChatCompat {
    val maxVoiceDistance: Double
    fun isPlayerDeafened(player: UUID): Boolean
    fun isPlayerMutedOrDeafened(player: UUID): Boolean
    fun isPlayerAudible(player: UUID): Boolean
    fun playerSharesGroup(player: UUID, other: UUID): Boolean

    fun getNearbyPlayers(source: UUID): List<UUID> {
        if (isPlayerMutedOrDeafened(source))
            return listOf(source)

        val distanceSq = maxVoiceDistance * maxVoiceDistance
        return instance.proxy.getAllPlayersInLevel(source).filter {
            (!isPlayerDeafened(it) && ((instance.proxy.getSqDistance(source, it) <= distanceSq && instance.proxy.canHearPlayer(source, it)) ||
                    playerSharesGroup(it, source)))
                || source == it
        }
    }

    companion object {
        val instance: UnityTranslate
            get() {
                return UnityTranslate.instance
            }
    }
}