package xyz.bluspring.unitytranslate.transcriber

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptSender

data class DirectTranscriptData(
    override val timeCreated: Long,
    override val sender: TranscriptSender,
    override val language: Language,
    override var message: String,
) : TranscriptData {
    override var timeUpdated: Long = this.timeCreated
}
