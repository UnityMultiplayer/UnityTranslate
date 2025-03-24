package xyz.bluspring.unitytranslate.common.network.v0.clientbound

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.*

data class V0MarkIncompletePacket(
    val fromLang: Language,
    val toLang: Language,
    val playerUUID: UUID,
    val index: Int,
    val isIncomplete: Boolean
) : UTPacket {
    override fun handleClient() {

    }
}