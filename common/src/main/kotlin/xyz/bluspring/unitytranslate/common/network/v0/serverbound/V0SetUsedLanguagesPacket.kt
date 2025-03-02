package xyz.bluspring.unitytranslate.common.network.v0.serverbound

import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.network.UTPacket
import java.util.EnumSet
import java.util.UUID

data class V0SetUsedLanguagesPacket(val languages: EnumSet<Language>) : UTPacket {
    override fun handleServer(player: UUID) {
        instance.serverNetworking.usedLanguages[player] = languages
    }
}