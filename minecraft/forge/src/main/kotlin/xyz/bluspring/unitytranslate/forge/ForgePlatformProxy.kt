package xyz.bluspring.unitytranslate.forge

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.server.permission.PermissionAPI
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.MinecraftPlatformProxy
import java.util.UUID

class ForgePlatformProxy : MinecraftPlatformProxy() {
    override val modVersion: String
        get() = ModList.get().getModFileById(UnityTranslate.MOD_ID).versionString()

    override fun isLoaded(name: String): Boolean {
        return ModList.get().isLoaded(name)
    }

    override fun isClient(): Boolean {
        return FMLLoader.getDist().isClient
    }

    override fun hasPermission(uuid: UUID, permission: String): Boolean {
        return PermissionAPI.getOfflinePermission(uuid, ForgePermissionsHandler.permissions[permission] ?: return false)
    }
}