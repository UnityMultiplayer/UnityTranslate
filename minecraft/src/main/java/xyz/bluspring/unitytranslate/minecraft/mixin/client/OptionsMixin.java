package xyz.bluspring.unitytranslate.minecraft.mixin.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.minecraft.client.KeybindHelper;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow @Final @Mutable
    public KeyMapping[] keyMappings;

    @Inject(method = "load", at = @At("HEAD"))
    private void ut$addUnityTranslateKeybinds(CallbackInfo ci) {
        keyMappings = KeybindHelper.INSTANCE.create(keyMappings);
    }
}
