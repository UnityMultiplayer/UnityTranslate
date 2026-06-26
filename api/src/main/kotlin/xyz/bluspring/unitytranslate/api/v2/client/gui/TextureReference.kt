package xyz.bluspring.unitytranslate.api.v2.client.gui

import com.mojang.serialization.Codec
import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.unitytranslate.client.renderer.ui.texture.AbstractTextureReference
import java.nio.file.Path

/**
 * A reference to a provided texture, usually used for UI rendering.
 */
@ApiStatus.NonExtendable
interface TextureReference {
    val imageWidth: Int
    val imageHeight: Int

    val width: Int
    val height: Int

    val u0: Float
    val v0: Float
    val u1: Float
    val v1: Float

    companion object {
        @JvmField val CODEC: Codec<TextureReference> = AbstractTextureReference.CODEC.xmap({ it as TextureReference }, { it as AbstractTextureReference })

        @JvmStatic fun file(path: Path): TextureReference = AbstractTextureReference.file(path) as TextureReference
        @JvmStatic fun minecraftTexture(id: String): TextureReference = AbstractTextureReference.minecraftTexture(id) as TextureReference
        @JvmStatic fun minecraftSprite(atlas: String, id: String): TextureReference = AbstractTextureReference.minecraftSprite(atlas, id) as TextureReference
    }
}
