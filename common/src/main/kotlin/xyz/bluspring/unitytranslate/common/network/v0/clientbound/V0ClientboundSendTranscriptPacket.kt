package xyz.bluspring.unitytranslate.common.network.v0.clientbound

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.*

data class V0ClientboundSendTranscriptPacket(
    val uuid: UUID,
    val language: Language,
    val index: Int,
    val updateTime: Long,
    val toSend: Map<Language, String>
) : UTPacket {
    override fun handleClient() {
        UnityTranslate.instance.proxy.handleTranscriptClient(uuid, language, index, updateTime, toSend)
    }
}