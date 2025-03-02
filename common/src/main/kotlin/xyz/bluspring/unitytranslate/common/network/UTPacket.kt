package xyz.bluspring.unitytranslate.common.network

import xyz.bluspring.unitytranslate.common.UnityTranslate
import java.util.*

interface UTPacket {
    val instance: UnityTranslate
        get() = UnityTranslate.instance

    fun handleServer(player: UUID) {
        throw IllegalStateException("Packet ${this::class.java} is not serverbound!")
    }

    fun handleClient() {
        throw IllegalStateException("Packet ${this::class.java} is not clientbound!")
    }
}