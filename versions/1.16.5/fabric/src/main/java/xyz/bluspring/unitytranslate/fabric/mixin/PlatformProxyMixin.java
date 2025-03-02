package xyz.bluspring.unitytranslate.fabric.mixin;

import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.bluspring.unitytranslate.common.PlatformProxy;
import xyz.bluspring.unitytranslate.common.network.PacketIds;
import xyz.bluspring.unitytranslate.common.network.UTPacket;
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy;

import java.util.UUID;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(value = PlatformProxy.class, remap = false)
@IfModLoaded("fabric-permissions-api-v0")
@IfModLoaded("fabric-networking-api-v1")
public abstract class PlatformProxyMixin {
    @Overwrite
    public boolean hasPermission(UUID uuid, String permission) {
        var player = MinecraftProxy.INSTANCE.getPlayer(uuid);
        if (player == null)
            return false;
        return Permissions.check(player, permission);
    }

    @IfMinecraftVersion(maxVersion = "1.20.4")
    @Overwrite
    public void sendPacketServer(UUID uuid, UTPacket packet) {
        var definition = PacketIds.INSTANCE.getPacketDefinition(packet);
        var buf = MinecraftProxy.INSTANCE.buildPacket(definition, packet);

        ServerPlayNetworking.send(MinecraftProxy.INSTANCE.getServer().getPlayerList().getPlayer(uuid), MinecraftProxy.INSTANCE.asPacketResource(definition.getId()), buf);
    }

    @IfMinecraftVersion(maxVersion = "1.20.4")
    @Overwrite
    public void broadcastPacketServer(UTPacket packet) {
        var definition = PacketIds.INSTANCE.getPacketDefinition(packet);
        var buf = MinecraftProxy.INSTANCE.buildPacket(definition, packet);
        var id = MinecraftProxy.INSTANCE.asPacketResource(definition.getId());

        for (ServerPlayer player : MinecraftProxy.INSTANCE.getServer().getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, id, buf);
        }
    }

    @IfMinecraftVersion(maxVersion = "1.20.4")
    @Environment(EnvType.CLIENT)
    @Overwrite
    public void sendPacketClient(UTPacket packet) {
        var definition = PacketIds.INSTANCE.getPacketDefinition(packet);
        var buf = MinecraftProxy.INSTANCE.buildPacket(definition, packet);
        var id = MinecraftProxy.INSTANCE.asPacketResource(definition.getId());

        ClientPlayNetworking.send(id, buf);
    }
}