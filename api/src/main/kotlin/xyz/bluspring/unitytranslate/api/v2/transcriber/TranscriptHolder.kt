package xyz.bluspring.unitytranslate.api.v2.transcriber

@JvmRecord
data class TranscriptHolder(
    val languageCode: String,
    val transcripts: MutableList<TranscriptData>
)
