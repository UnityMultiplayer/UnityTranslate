package xyz.bluspring.unitytranslate.network

import net.minecraft.client.Minecraft
import xyz.bluspring.modernnetworking.api.minecraft.VanillaPacketSender
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.network.payloads.SetCurrentLanguagePayload
import xyz.bluspring.unitytranslate.network.payloads.SetUsedLanguagesPayload
import java.util.*

object UTClientNetworking {
    val proxy = UnityTranslate.instance.proxy
    val registry = PacketDefinitions.registry

    fun init() {
        registry.addClientboundHandler(PacketDefinitions.SERVER_SUPPORT) { packet, ctx ->
            UnityTranslateClient.connectedServerHasSupport = true
        }

        registry.addClientboundHandler(PacketDefinitions.SEND_TRANSCRIPT_TO_CLIENT) { packet, ctx ->
            val sourceId = packet.uuid
            val source = ctx.player.level().getPlayerByUUID(sourceId) ?: return@addClientboundHandler

            val sourceLanguage = packet.language
            val index = packet.index
            val updateTime = packet.updateTime

            val holders = UnityTranslateClient.transcriptHolders

            for ((language, text) in packet.toSend) {
                if (language == UnityTranslateClient.transcriber.language && sourceId == ctx.player?.uuid)
                    continue

                val box = holders.firstOrNull { it.language == language }
                box?.updateTranscript(source, text, sourceLanguage, index, updateTime, false)
            }
        }

        registry.addClientboundHandler(PacketDefinitions.MARK_INCOMPLETE) { packet, ctx ->
            val from = packet.from
            val to = packet.to
            val uuid = packet.uuid
            val index = packet.index
            val isIncomplete = packet.isIncomplete

            val box = UnityTranslateClient.transcriptHolders.firstOrNull { it.language == to } ?: return@addClientboundHandler
            box.transcripts.firstOrNull { it.language == from && it.player.uuid == uuid && it.index == index }?.incomplete = isIncomplete
        }
    }

    fun onClientJoin() {
        Minecraft.getInstance().execute {
            VanillaPacketSender.sendToServer(SetUsedLanguagesPayload(EnumSet.copyOf(UnityTranslateClient.transcriptHolders.map { it.language })))
            VanillaPacketSender.sendToServer(SetCurrentLanguagePayload(UnityTranslate.config.client.language))
        }
    }

    fun onClientLeave() {
        UnityTranslateClient.connectedServerHasSupport = false
    }

    fun updateLanguagesToServer() {
        if (Minecraft.getInstance().player != null) {
            VanillaPacketSender.sendToServer(SetUsedLanguagesPayload(EnumSet.copyOf(UnityTranslateClient.transcriptHolders.map { it.language })))
        }
    }
}