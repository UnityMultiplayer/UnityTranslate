package xyz.bluspring.unitytranslate.common.translator

import xyz.bluspring.unitytranslate.common.Language
import java.util.*

data class Transcript(
    val index: Int,
    val player: UUID,
    val playerRef: Any,
    var text: String,
    val language: Language,
    var lastUpdateTime: Long,
    var incomplete: Boolean,

    var arrivalTime: Long = System.currentTimeMillis()
)

