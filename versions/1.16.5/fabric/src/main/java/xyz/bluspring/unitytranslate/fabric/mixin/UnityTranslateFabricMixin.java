package xyz.bluspring.unitytranslate.fabric.mixin;

import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.fabric.UnityTranslateFabric;
import xyz.bluspring.unitytranslate.fabric.network.UTServerNetworkHandler;

@Pseudo
@Mixin(value = UnityTranslateFabric.class, remap = false)
public abstract class UnityTranslateFabricMixin {
    @Dynamic
    @IfMinecraftVersion(maxVersion = "1.20.4")
    @Inject(method = "onInitialize", at = @At("TAIL"))
    private void initServerNetworking(CallbackInfo ci) {
        UTServerNetworkHandler.INSTANCE.init();
    }
}
