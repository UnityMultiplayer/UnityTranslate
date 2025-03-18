package xyz.bluspring.unitytranslate.minecraft.mixin;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.bluspring.unitytranslate.common.Language;
import xyz.bluspring.unitytranslate.common.PlatformProxy;
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(value = PlatformProxy.class, remap = false)
public abstract class PlatformProxyMixin {
    /**
     * @author BluSpring
     * @reason The translation system is incredibly similar across all versions, so this mixin is pretty safe.
     */
    @Overwrite
    public String getTranslation(String key, String ...args) {
        return I18n.get(key, (Object[]) args);
    }

    @Overwrite
    public List<UUID> getAllPlayersInLevel(UUID uuid) {
        var player = MinecraftProxy.INSTANCE.getPlayer(uuid);
        if (player == null)
            return List.of();

        var level = MinecraftProxy.INSTANCE.getEntityLevel(player);
        if (level == null)
            return List.of();

        return level.players().stream().map(Entity::getUUID).toList();
    }

    @Overwrite
    public double getSqDistance(UUID playerUUID, UUID otherUUID) {
        var player = MinecraftProxy.INSTANCE.getPlayer(playerUUID);
        var other = MinecraftProxy.INSTANCE.getPlayer(otherUUID);

        if (player == null || other == null)
            return 13000;

        return player.distanceToSqr(other);
    }

    @Overwrite
    public void setClientPlayerLanguage(Language language) {

    }

    @Overwrite
    public boolean doesPlayerExist(UUID uuid) {
        return MinecraftProxy.INSTANCE.getPlayer(uuid) != null;
    }

    @Overwrite
    public void queue(Runnable runnable) {
        MinecraftProxy.INSTANCE.getServer().execute(runnable);
    }
}
