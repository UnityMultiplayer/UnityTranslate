package xyz.bluspring.unitytranslate.client.transcriber.sender

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptSender

object MinecraftLocalTranscriptSender : TranscriptSender {
    override val displayName: Component
        get() = Minecraft.getInstance().player?.displayName ?: Component.literal(Minecraft.getInstance().gameProfile.name)
}
