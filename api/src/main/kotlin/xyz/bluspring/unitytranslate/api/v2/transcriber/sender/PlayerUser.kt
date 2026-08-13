package xyz.bluspring.unitytranslate.api.v2.transcriber.sender

import org.joml.Vector3f
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import java.util.*

/**
 * A Minecraft player as a [TranscriptUser].
 */
@JvmRecord
data class PlayerUser(
    /**
     * The UUID of the Minecraft player.
     */
    val uuid: UUID,
    override val pos: Vector3f,

    /**
     * The Minecraft player's display name. This may not match their username,
     * and as such it is up to the implementation to look up the player's username
     * using the [uuid].
     */
    override val displayName: TextComponent,
) : TranscriptUser {

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is UUID) return this.uuid == other
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerUser

        return uuid == other.uuid
    }
}
