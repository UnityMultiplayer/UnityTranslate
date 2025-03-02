package xyz.bluspring.unitytranslate.common.network.v0.serverbound

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.UUID

data class V0SetCurrentLanguagePacket(val language: Language) : UTPacket {
    override fun handleServer(player: UUID) {
        instance.proxy.setPlayerLanguage(player, this.language)
    }
}