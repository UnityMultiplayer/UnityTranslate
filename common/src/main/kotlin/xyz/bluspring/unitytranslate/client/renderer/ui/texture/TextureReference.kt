package xyz.bluspring.unitytranslate.client.renderer.ui.texture

import com.mojang.blaze3d.textures.GpuTextureView
import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.PlatformProxy

abstract class TextureReference(val type: String) {
    abstract val textureView: GpuTextureView

    abstract val imageWidth: Int
    abstract val imageHeight: Int

    open val width: Int
        get() = this.imageWidth

    open val height: Int
        get() = this.imageHeight

    open val u0: Float = 0f
    open val v0: Float = 0f
    open val u1: Float = 1f
    open val v1: Float = 1f

    companion object {
        @JvmField val CODEC: Codec<TextureReference> = Codec.STRING.dispatch("type", TextureReference::type) { type ->
            when (type) {
                "direct" -> throw IllegalArgumentException("Direct texture reference is not allowed to be serialized!")
                "file" -> FileTextureReference.CODEC
                "minecraft_sprite" -> {
                    if (PlatformProxy.instance.isStandalone())
                        throw IllegalArgumentException("Minecraft sprites are not allowed to be used in standalone mode!")

                    MinecraftSpriteTextureReference.CODEC
                }
                "minecraft_texture" -> {
                    if (PlatformProxy.instance.isStandalone())
                        throw IllegalArgumentException("Minecraft textures are not allowed to be used in standalone mode!")

                    MinecraftTextureReference.CODEC
                }
                else -> throw IllegalArgumentException("Unknown texture reference type ${type}!")
            }
        }
    }
}
