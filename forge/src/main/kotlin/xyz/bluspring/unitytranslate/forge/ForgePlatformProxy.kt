package xyz.bluspring.unitytranslate.forge

import net.minecraft.world.entity.player.Player
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.server.permission.PermissionAPI
import xyz.bluspring.unitytranslate.PlatformProxy
import java.nio.file.Path

class ForgePlatformProxy : PlatformProxy {
    lateinit var container: ModContainer

    override fun isModLoaded(id: String): Boolean {
        return ModList.get().isLoaded(id)
    }

    override fun isClient(): Boolean {
        return FMLEnvironment.dist == Dist.CLIENT
    }

    override val modVersion: String
        get() = container.modInfo.version.toString()

    override val configDir: Path
        get() = FMLPaths.CONFIGDIR.get()

    override val gameDir: Path
        get() = FMLPaths.GAMEDIR.get()

    override val isDev: Boolean
        get() = !FMLEnvironment.production

    override fun hasTranscriptPermission(player: Player): Boolean {
        return PermissionAPI.getOfflinePermission(player.uuid, UnityTranslateForge.REQUEST_TRANSLATIONS_NODE)
    }
}