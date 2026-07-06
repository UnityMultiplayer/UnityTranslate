package xyz.bluspring.unitytranslate.api.v2.transcriber

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptSender

/**
 * The data for each transcript. Usually stored in [TranscriptHolder.transcripts].
 */
interface TranscriptData {
    /**
     * The timestamp, in ms, of when the transcript was first created.
     *
     * When translated, this value will still match the original untranslated data's value, even if the translation
     * arrived later.
     */
    val timeCreated: Long

    /**
     * The sender who sent this transcript.
     */
    val sender: TranscriptSender

    /**
     * The original language of the transcript, before it was translated.
     */
    val language: Language

    /**
     * The timestamp, in ms, of when the transcript was last updated.
     * This value will be used for sorting the transcript in a transcript holder list.
     *
     * When translated, this value will still match the original untranslated data's value, even if the translation
     * arrived later.
     */
    val timeUpdated: Long

    /**
     * The message contained in this data.
     */
    val message: String

    companion object {
        /**
         * A unique ID used to help identify the transcript sent, so it can be updated on the players'
         * transcript boxes.
         */
        @JvmStatic
        val TranscriptData.id: String
            get() = "$sender/$timeCreated"
    }
}
