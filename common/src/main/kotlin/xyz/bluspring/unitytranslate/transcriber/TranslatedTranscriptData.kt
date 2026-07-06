package xyz.bluspring.unitytranslate.transcriber

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptSender

@JvmRecord
data class TranslatedTranscriptData(
    override val timeCreated: Long,
    override val sender: TranscriptSender,
    override val language: Language,
    val original: String,
    override val message: String,
    override val timeUpdated: Long,
) : TranscriptData
