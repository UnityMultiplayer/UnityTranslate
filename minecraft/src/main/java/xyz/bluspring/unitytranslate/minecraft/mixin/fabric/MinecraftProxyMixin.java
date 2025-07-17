package xyz.bluspring.unitytranslate.minecraft.mixin.fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy;

import java.nio.file.Path;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(MinecraftProxy.class)
@IfModLoaded("fabricloader")
public abstract class MinecraftProxyMixin {
    //? if fabric {
    @Overwrite(remap = false)
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }
    //?}
}
