package xyz.bluspring.unitytranslate.api.v2.transcriber.sender

import net.minecraft.network.chat.Component
import java.util.*

/**
 * A Minecraft player as a [TranscriptSender].
 */
@JvmRecord
data class PlayerSender(
    /**
     * The UUID of the Minecraft player.
     */
    val uuid: UUID,

    /**
     * The Minecraft player's display name. This may not match their username,
     * and as such it is up to the implementation to look up the player's username
     * using the [uuid].
     */
    override val displayName: Component,
) : TranscriptSender
