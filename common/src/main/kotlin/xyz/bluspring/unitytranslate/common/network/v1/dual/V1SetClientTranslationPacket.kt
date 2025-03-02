package xyz.bluspring.unitytranslate.common.network.v1.dual

import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.UUID

data class V1SetClientTranslationPacket(val enabled: Boolean) : UTPacket {
    override fun handleClient() {
        throw IllegalStateException("No client implementation has been implemented! This is supposed to be modified via mixin!")
    }

    override fun handleServer(player: UUID) {
        val clientTranslators = instance.serverNetworking.clientTranslators

        if (!clientTranslators.contains(player) && enabled)
            clientTranslators.add(player)
        else if (clientTranslators.contains(player) && !enabled)
            clientTranslators.remove(player)
    }
}