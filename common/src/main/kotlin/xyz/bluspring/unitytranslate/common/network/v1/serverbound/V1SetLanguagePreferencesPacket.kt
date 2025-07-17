package xyz.bluspring.unitytranslate.common.network.v1.serverbound

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets
import java.util.EnumSet

data class V1SetLanguagePreferencesPacket(
    val languages: EnumSet<Language>
) : UTPacket {
    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.SET_LANGUAGE_PREFERENCES

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.enumSetCodec(Language::class.java), V1SetLanguagePreferencesPacket::languages,
            ::V1SetLanguagePreferencesPacket
        )
    }
}
