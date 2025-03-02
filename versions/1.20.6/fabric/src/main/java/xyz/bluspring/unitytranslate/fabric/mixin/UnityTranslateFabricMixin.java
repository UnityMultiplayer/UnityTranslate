package xyz.bluspring.unitytranslate.fabric.mixin;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.fabric.UnityTranslateFabric;
import xyz.bluspring.unitytranslate.minecraft.mc12006.UTModernNetworkHelper;

@Mixin(value = UnityTranslateFabric.class, remap = false)
public class UnityTranslateFabricMixin {
    @Dynamic
    @Inject(method = "onInitialize", at = @At("TAIL"))
    private void loadModernNetworkHelper(CallbackInfo ci) {
        UTModernNetworkHelper.INSTANCE.init();
    }
}
