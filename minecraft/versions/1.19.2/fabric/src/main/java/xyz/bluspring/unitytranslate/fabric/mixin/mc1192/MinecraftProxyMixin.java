package xyz.bluspring.unitytranslate.fabric.mixin.mc1192;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(MinecraftProxy.class)
public class MinecraftProxyMixin {
    @Overwrite
    public Component literal(String text) {
        return Component.literal(text);
    }

    @Overwrite
    public Component translatable(String text) {
        return Component.translatable(text);
    }
}
