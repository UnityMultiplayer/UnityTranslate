package xyz.bluspring.unitytranslate.translator

import kotlinx.coroutines.CompletableDeferred
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.Language

data class Translation(
    val id: String, // follows "playerID-transcriptIndex"
    val text: String,
    val fromLang: Language,
    val toLang: Language,
    val queueTime: Long,
    val future: CompletableDeferred<String>,
    val player: Player,
    val index: Int,
    val chunk: Int
) {
    var attempts = 0
}