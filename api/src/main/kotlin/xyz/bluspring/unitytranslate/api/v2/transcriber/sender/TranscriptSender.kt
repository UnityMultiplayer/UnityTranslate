package xyz.bluspring.unitytranslate.api.v2.transcriber.sender

import net.minecraft.network.chat.Component

/**
 * Represents the sender of a transcript.
 */
interface TranscriptSender {
    /**
     * How the transcript's sender will be displayed in a transcript box.
     */
    val displayName: Component
}
