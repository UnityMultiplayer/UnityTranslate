package xyz.bluspring.unitytranslate.common.translator

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.holders.PlayerHolder

data class Transcript(
    val index: Int,
    val player: PlayerHolder,
    var text: String,
    val language: Language,
    var lastUpdateTime: Long,
    var incomplete: Boolean,

    var arrivalTime: Long = System.currentTimeMillis()
)

