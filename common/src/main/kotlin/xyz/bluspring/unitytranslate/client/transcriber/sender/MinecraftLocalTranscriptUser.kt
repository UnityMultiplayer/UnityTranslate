package xyz.bluspring.unitytranslate.client.transcriber.sender

import net.minecraft.client.Minecraft
import org.joml.Vector3f
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.api.v2.transcriber.sender.TranscriptUser
import xyz.bluspring.unitytranslate.util.PlatformConversion.asUnityTranslate

object MinecraftLocalTranscriptUser : TranscriptUser {
    override val displayName: TextComponent
        get() = Minecraft.getInstance().player?.displayName?.asUnityTranslate() ?: TextComponent.literal(Minecraft.getInstance().gameProfile.name)

    override val pos: Vector3f?
        get() = Minecraft.getInstance().player?.position()?.toVector3f()
}
