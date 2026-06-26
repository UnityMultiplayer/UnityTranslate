package xyz.bluspring.unitytranslate.client.renderer.ui.texture;

import java.nio.file.Path;

import com.mojang.serialization.Codec;

public abstract class AbstractTextureReference {
    public static final Codec<AbstractTextureReference> CODEC = null;

    public static AbstractTextureReference file(Path path) {
        throw new IllegalStateException();
    }

    public static AbstractTextureReference minecraftTexture(String id) {
        throw new IllegalStateException();
    }

    public static AbstractTextureReference minecraftSprite(String atlas, String id) {
        throw new IllegalStateException();
    }
}
