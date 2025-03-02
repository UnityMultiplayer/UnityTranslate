package xyz.bluspring.unitytranslate.common.network.v1.serverbound

import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.UUID

data class V1RequestVersionPacket(
    val protocolVersion: Int,
    val modVersion: String
) : UTPacket {
    override fun handleServer(player: UUID) {
        instance.serverNetworking.maximumProtocolVersions[player] = protocolVersion
        instance.serverNetworking.modVersions[player] = modVersion
    }
}