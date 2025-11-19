package xyz.bluspring.unitytranslate.network.payloads

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.network.PacketDefinitions
import java.util.*

data class SetUsedLanguagesPayload(
    val languages: EnumSet<Language>
) : NetworkPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = PacketDefinitions.SET_USED_LANGUAGES

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.enumSetCodec(Language::class.java), SetUsedLanguagesPayload::languages,

            ::SetUsedLanguagesPayload
        )
    }
}