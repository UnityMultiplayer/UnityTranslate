package xyz.bluspring.unitytranslate.minecraft.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ut$setActiveServer(CallbackInfo ci) {
        MinecraftProxy.INSTANCE.setServer((MinecraftServer) (Object) this);
    }
}
