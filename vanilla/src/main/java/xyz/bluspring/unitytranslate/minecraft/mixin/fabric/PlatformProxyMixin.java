package xyz.bluspring.unitytranslate.minecraft.mixin.fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.bluspring.unitytranslate.common.PlatformProxy;
import xyz.bluspring.unitytranslate.common.UnityTranslate;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(value = PlatformProxy.class, remap = false)
@IfModLoaded("fabricloader")
public abstract class PlatformProxyMixin {
    @Overwrite
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Overwrite
    public boolean isLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    @Overwrite
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(UnityTranslate.MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
    }
}
