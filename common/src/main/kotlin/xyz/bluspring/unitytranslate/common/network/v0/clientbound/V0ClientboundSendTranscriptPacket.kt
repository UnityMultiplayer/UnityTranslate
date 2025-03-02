package xyz.bluspring.unitytranslate.common.network.v0.clientbound

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.UUID

data class V0ClientboundSendTranscriptPacket(
    val uuid: UUID,
    val language: Language,
    val index: Int,
    val updateTime: Long,
    val toSend: Map<Language, String>
) : UTPacket