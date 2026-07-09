package xyz.bluspring.unitytranslate.client.transcriber.sender

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import org.joml.Vector3f
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser

object MinecraftLocalTranscriptUser : TranscriptUser {
    override val displayName: Component
        get() = Minecraft.getInstance().player?.displayName ?: Component.literal(Minecraft.getInstance().gameProfile.name)

    override val pos: Vector3f?
        get() = Minecraft.getInstance().player?.position()?.toVector3f()
}
