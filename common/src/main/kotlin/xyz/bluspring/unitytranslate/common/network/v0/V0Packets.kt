package xyz.bluspring.unitytranslate.common.network.v0

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.PacketBuilder
import xyz.bluspring.unitytranslate.common.network.PacketDefinitions
import xyz.bluspring.unitytranslate.common.network.v0.clientbound.V0ClientboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v0.clientbound.V0MarkIncompletePacket
import xyz.bluspring.unitytranslate.common.network.v0.clientbound.V0ServerSupportPacket
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0ServerboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0SetCurrentLanguagePacket
import xyz.bluspring.unitytranslate.common.network.v0.serverbound.V0SetUsedLanguagesPacket

object V0Packets : PacketDefinitions {
    val SERVER_SUPPORT = PacketBuilder("server_support", PacketBuilder.Direction.CLIENTBOUND, ::V0ServerSupportPacket)
    val SEND_TRANSCRIPT_SERVERBOUND = PacketBuilder("send_transcript", PacketBuilder.Direction.SERVERBOUND,
        ::V0ServerboundSendTranscriptPacket
    )
        .addEnum(Language::class.java, V0ServerboundSendTranscriptPacket::sourceLanguage)
        .addType(PacketBuilder.DataType.STRING, V0ServerboundSendTranscriptPacket::text)
        .addType(PacketBuilder.DataType.VAR_INT, V0ServerboundSendTranscriptPacket::index)
        .addType(PacketBuilder.DataType.VAR_LONG, V0ServerboundSendTranscriptPacket::updateTime)

    val SEND_TRANSCRIPT_CLIENTBOUND = PacketBuilder("send_transcript", PacketBuilder.Direction.CLIENTBOUND,
        ::V0ClientboundSendTranscriptPacket
    )
        .addType(PacketBuilder.DataType.UUID, V0ClientboundSendTranscriptPacket::uuid)
        .addEnum(Language::class.java, V0ClientboundSendTranscriptPacket::language)
        .addType(PacketBuilder.DataType.VAR_INT, V0ClientboundSendTranscriptPacket::index)
        .addType(PacketBuilder.DataType.VAR_LONG, V0ClientboundSendTranscriptPacket::updateTime)
        .addEnumKeyMap(Language::class.java, PacketBuilder.DataType.STRING, V0ClientboundSendTranscriptPacket::toSend)

    val SET_USED_LANGUAGES = PacketBuilder("set_used_languages", PacketBuilder.Direction.SERVERBOUND,
        ::V0SetUsedLanguagesPacket
    )
        .addEnumSet(PacketBuilder.DataType.ENUM_SET, Language::class.java, V0SetUsedLanguagesPacket::languages)

    //val TRANSLATE_SIGN = PacketBuilder("translate_sign", PacketBuilder.Direction.SERVERBOUND, ::V0TranslateSignPacket)

    val SET_CURRENT_LANGUAGE = PacketBuilder("set_current_language", PacketBuilder.Direction.SERVERBOUND,
        ::V0SetCurrentLanguagePacket
    )
        .addEnum(Language::class.java, V0SetCurrentLanguagePacket::language)

    val MARK_INCOMPLETE = PacketBuilder("mark_incomplete", PacketBuilder.Direction.CLIENTBOUND,
        ::V0MarkIncompletePacket
    )
        .addEnum(Language::class.java, V0MarkIncompletePacket::fromLang)
        .addEnum(Language::class.java, V0MarkIncompletePacket::toLang)
        .addType(PacketBuilder.DataType.UUID, V0MarkIncompletePacket::playerUUID)
        .addType(PacketBuilder.DataType.VAR_INT, V0MarkIncompletePacket::index)
        .addType(PacketBuilder.DataType.BOOLEAN, V0MarkIncompletePacket::isIncomplete)

    override val packets = setOf<PacketBuilder<*>>(
        SERVER_SUPPORT,
        SEND_TRANSCRIPT_SERVERBOUND,
        SEND_TRANSCRIPT_CLIENTBOUND,
        SET_USED_LANGUAGES,
        //TRANSLATE_SIGN,
        SET_CURRENT_LANGUAGE,
        MARK_INCOMPLETE
    )
}