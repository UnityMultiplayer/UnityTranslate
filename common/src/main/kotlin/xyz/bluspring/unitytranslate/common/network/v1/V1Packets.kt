package xyz.bluspring.unitytranslate.common.network.v1

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketDefinitions
import xyz.bluspring.unitytranslate.common.network.v1.dual.V1SyncConfigPacket
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1RequestVersionPacket
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1SendClientTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v1.dual.V1SetClientTranslationPacket

object V1Packets : PacketDefinitions {
    val SEND_CLIENT_TRANSCRIPT = PacketBuilder("v1/send_client_transcript", PacketBuilder.Direction.SERVERBOUND, ::V1SendClientTranscriptPacket)
        .addEnum(Language::class.java, V1SendClientTranscriptPacket::sourceLanguage)
        .addType(PacketBuilder.DataType.VAR_INT, V1SendClientTranscriptPacket::index)
        .addType(PacketBuilder.DataType.VAR_LONG, V1SendClientTranscriptPacket::updateTime)
        .addEnumKeyMap(Language::class.java, PacketBuilder.DataType.STRING, V1SendClientTranscriptPacket::translated)

    val SYNC_CONFIG = PacketBuilder("v1/sync_config", PacketBuilder.Direction.DUAL, ::V1SyncConfigPacket)
        .addType(PacketBuilder.DataType.STRING, V1SyncConfigPacket::serverData)
        .addType(PacketBuilder.DataType.STRING, V1SyncConfigPacket::commonData)

    val SET_CLIENT_TRANSLATION = PacketBuilder("v1/set_client_translation", PacketBuilder.Direction.DUAL, ::V1SetClientTranslationPacket)
        .addType(PacketBuilder.DataType.BOOLEAN, V1SetClientTranslationPacket::enabled)

    val REQUEST_VERSION = PacketBuilder("v1/request_version", PacketBuilder.Direction.SERVERBOUND, ::V1RequestVersionPacket)
        .addType(PacketBuilder.DataType.VAR_INT, V1RequestVersionPacket::protocolVersion)
        .addType(PacketBuilder.DataType.STRING, V1RequestVersionPacket::modVersion)

    override val packets = setOf(
        SEND_CLIENT_TRANSCRIPT,
        SYNC_CONFIG,
        SET_CLIENT_TRANSLATION,
        REQUEST_VERSION
    )
}