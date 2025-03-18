package xyz.bluspring.unitytranslate.minecraft.network

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0ServerboundSendTranscriptPacket

object UTClientNetworkSender {
    fun sendTranscriptToServer(language: Language, text: String, index: Int, updateTime: Long) {
        UnityTranslate.instance.proxy.sendPacketClient(V0ServerboundSendTranscriptPacket(language, text, index, updateTime))
    }
}