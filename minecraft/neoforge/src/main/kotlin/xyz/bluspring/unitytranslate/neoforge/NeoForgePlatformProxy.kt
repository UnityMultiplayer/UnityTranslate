package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.neoforge.server.permission.PermissionAPI
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.MinecraftPlatformProxy
import java.util.UUID

class NeoForgePlatformProxy : MinecraftPlatformProxy() {
    override val modVersion: String
        get() = ModList.get().getModFileById(UnityTranslate.MOD_ID).versionString()

    override fun isLoaded(name: String): Boolean {
        return ModList.get().isLoaded(name)
    }

    override fun isClient(): Boolean {
        return FMLLoader.getDist().isClient
    }

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        return PermissionAPI.getOfflinePermission(uuid, NeoForgePermissionsHandler.permissions[permission] ?: return false)
    }
}