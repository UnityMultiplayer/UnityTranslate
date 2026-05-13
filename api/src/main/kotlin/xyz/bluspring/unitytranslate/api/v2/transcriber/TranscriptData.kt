package xyz.bluspring.unitytranslate.api.v2.transcriber

import net.minecraft.network.chat.Component
import java.util.*

interface TranscriptData {
    val id: String
    val sender: UUID
    val displayName: Component
    val languageCode: String
    var message: String
}
