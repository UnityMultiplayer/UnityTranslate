package xyz.bluspring.unitytranslate.fabric.mixin;

import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.fabric.client.UnityTranslateFabricClient;
import xyz.bluspring.unitytranslate.fabric.network.UTClientNetworkHandler;

@Pseudo
@Mixin(value = UnityTranslateFabricClient.class, remap = false)
public abstract class UnityTranslateFabricClientMixin {
    @Dynamic
    @IfMinecraftVersion(maxVersion = "1.20.4")
    @Inject(method = "onInitializeClient", at = @At("TAIL"))
    private void initClientNetworking(CallbackInfo ci) {
        UTClientNetworkHandler.INSTANCE.init();
    }
}
