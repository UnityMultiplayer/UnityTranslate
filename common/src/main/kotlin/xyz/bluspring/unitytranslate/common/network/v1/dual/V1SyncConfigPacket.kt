package xyz.bluspring.unitytranslate.common.network.v1.dual

import io.netty.buffer.ByteBuf
import xyz.bluspring.modernnetworking.api.CompositeCodecs
import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.modernnetworking.api.NetworkPacket
import xyz.bluspring.modernnetworking.api.PacketDefinition
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.network.UTPacket
import xyz.bluspring.unitytranslate.common.network.v1.V1Packets
import xyz.bluspring.unitytranslate.common.util.Permissions
import java.util.UUID

data class V1SyncConfigPacket(
    val serverData: String,
    val commonData: String
) : UTPacket {
    override fun handleClient() {
        throw IllegalStateException("No client implementation has been implemented! This is supposed to be modified via mixin!")
    }

    override fun handleServer(player: UUID) {
        if (!instance.proxy.hasPermission(player, Permissions.MODIFY_CONFIG))
            return

        instance.config.common = UnityTranslate.json.decodeFromString(commonData)
        instance.config.server = UnityTranslate.json.decodeFromString(serverData)
        instance.saveConfig()
        instance.translatorManager.loadFromConfig()
    }

    override val definition: PacketDefinition<out NetworkPacket, out ByteBuf>
        get() = V1Packets.SYNC_CONFIG

    companion object {
        val CODEC = CompositeCodecs.composite(
            NetworkCodecs.STRING_UTF8, V1SyncConfigPacket::serverData,
            NetworkCodecs.STRING_UTF8, V1SyncConfigPacket::commonData,
            ::V1SyncConfigPacket
        )
    }
}