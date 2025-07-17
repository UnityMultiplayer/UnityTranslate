package xyz.bluspring.unitytranslate.fabric

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.MinecraftPlatformProxy
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import java.util.UUID

class FabricPlatformProxy : MinecraftPlatformProxy() {
    override val modVersion: String
        get() = FabricLoader.getInstance().getModContainer(UnityTranslate.MOD_ID).orElseThrow().metadata.version.friendlyString

    override fun isLoaded(name: String): Boolean {
        return FabricLoader.getInstance().isModLoaded(name)
    }

    override fun isClient(): Boolean {
        return FabricLoader.getInstance().environmentType == EnvType.CLIENT
    }

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        val player = MinecraftProxy.getPlayer(uuid) ?: return false
        return Permissions.check(player, permission, true)
    }
}