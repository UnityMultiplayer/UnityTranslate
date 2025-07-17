package xyz.bluspring.unitytranslate.common.network.v1

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.AbstractNetworkRegistry
import xyz.bluspring.modernnetworking.api.NetworkCodec
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketDefinitions
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1ClientboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1MarkIncompletePacket
import xyz.bluspring.unitytranslate.common.network.v1.clientbound.V1ResponseVersionPacket
import xyz.bluspring.unitytranslate.common.network.v1.dual.V1SyncConfigPacket
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1RequestVersionPacket
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1SendClientTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v1.dual.V1SetClientTranslationPacket
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1ServerboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1SetLanguagePreferencesPacket

object V1Packets : PacketDefinitions {
    private fun <T : NetworkPacket, B : ByteBuf> createDefinition(id: String, codec: NetworkCodec<T, B>): PacketDefinition<T, B> {
        return PacketDefinition(UnityTranslate.MOD_ID, "v1/$id", codec)
    }

    val SEND_CLIENT_TRANSCRIPT = createDefinition("send_client_transcript", V1SendClientTranscriptPacket.CODEC)
    val SEND_TRANSCRIPT_SERVERBOUND = createDefinition("send_transcript", V1ServerboundSendTranscriptPacket.CODEC)
    val SEND_TRANSCRIPT_CLIENTBOUND = createDefinition("send_transcript", V1ClientboundSendTranscriptPacket.CODEC)
    val RESPONSE_VERSION = createDefinition("response_version", V1ResponseVersionPacket.CODEC)
    val SET_LANGUAGE_PREFERENCES = createDefinition("set_language_preferences", V1SetLanguagePreferencesPacket.CODEC)

    val SYNC_CONFIG = createDefinition("sync_config", V1SyncConfigPacket.CODEC)
    val SET_CLIENT_TRANSLATION = createDefinition("set_client_translation", V1SetClientTranslationPacket.CODEC)

    val REQUEST_VERSION = createDefinition("request_version", V1RequestVersionPacket.CODEC)
    val MARK_INCOMPLETE = createDefinition("mark_incomplete", V1MarkIncompletePacket.CODEC)

    override val clientboundPackets: Set<PacketDefinition<out UTPacket, out ByteBuf>> = setOf(
        SEND_TRANSCRIPT_CLIENTBOUND,
        RESPONSE_VERSION
    )

    override val serverboundPackets = setOf(
        SEND_CLIENT_TRANSCRIPT,
        REQUEST_VERSION,
        SEND_TRANSCRIPT_SERVERBOUND
    )

    override val dualPackets = setOf(
        SYNC_CONFIG,
        SET_CLIENT_TRANSLATION
    )
}