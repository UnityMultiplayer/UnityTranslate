package xyz.bluspring.unitytranslate.api.v2.transcriber.sender

import net.minecraft.network.chat.Component
import org.joml.Vector3f

/**
 * Represents the user of a transcript.
 */
interface TranscriptUser {
    /**
     * How the transcript's user will be displayed in a transcript box.
     */
    val displayName: Component

    /**
     * The position of the transcript's user. Null if the user does not support position data.
     */
    val pos: Vector3f?
}
