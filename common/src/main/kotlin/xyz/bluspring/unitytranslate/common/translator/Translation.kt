package xyz.bluspring.unitytranslate.common.translator

import kotlinx.coroutines.CompletableDeferred
import xyz.bluspring.unitytranslate.common.Language
import java.util.*

data class Translation(
    val id: String, // follows "playerID-transcriptIndex"
    val text: String,
    val fromLang: Language,
    val toLang: Language,
    val queueTime: Long,
    val job: CompletableDeferred<String>,
    val playerUUID: UUID,
    val index: Int
) {
    var attempts = 0
}
