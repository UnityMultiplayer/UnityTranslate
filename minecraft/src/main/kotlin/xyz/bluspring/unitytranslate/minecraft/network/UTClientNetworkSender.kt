package xyz.bluspring.unitytranslate.minecraft.network

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1ServerboundSendTranscriptPacket

object UTClientNetworkSender {
    fun sendTranscriptToServer(language: Language, text: String, index: Int, updateTime: Long) {
        UnityTranslate.instance.proxy.sendPacketClient(V1ServerboundSendTranscriptPacket(language, index, updateTime, text))
    }
}