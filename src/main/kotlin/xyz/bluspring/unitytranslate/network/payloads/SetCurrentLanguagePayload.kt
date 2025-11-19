package xyz.bluspring.unitytranslate.network.payloads

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketDefinitions

data class SetCurrentLanguagePayload(
    val language: Language
) : NetworkPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = PacketDefinitions.SET_CURRENT_LANGUAGE

    companion object {
        val CODEC = CompositeCodecs.composite(
            Language.NETWORK_CODEC, SetCurrentLanguagePayload::language,
            ::SetCurrentLanguagePayload
        )
    }
}