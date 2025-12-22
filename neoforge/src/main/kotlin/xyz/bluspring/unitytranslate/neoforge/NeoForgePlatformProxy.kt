package xyz.bluspring.unitytranslate.neoforge

import net.minecraft.world.entity.player.Player
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModContainer
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.server.permission.PermissionAPI
import xyz.bluspring.unitytranslate.PlatformProxy
import java.nio.file.Path

class NeoForgePlatformProxy : PlatformProxy {
    lateinit var container: ModContainer

    override fun isModLoaded(id: String): Boolean {
        return ModList.get().isLoaded(id)
    }

    override fun isClient(): Boolean {
        //? if > 1.21.8 {
        return FMLEnvironment.getDist() == Dist.CLIENT
        //?} else {
        /*return FMLEnvironment.dist == Dist.CLIENT
        *///?}
    }

    override val modVersion: String
        get() = container.modInfo.version.toString()

    override val configDir: Path
        get() = FMLPaths.CONFIGDIR.get()

    override val gameDir: Path
        get() = FMLPaths.GAMEDIR.get()

    override val isDev: Boolean
        get() {
            //? if > 1.21.8 {
            return !FMLEnvironment.isProduction()
            //?} else {
            /*return !FMLEnvironment.production
            *///?}
        }

    override fun hasTranscriptPermission(player: Player): Boolean {
        return PermissionAPI.getOfflinePermission(player.uuid, UnityTranslateNeoForge.REQUEST_TRANSLATIONS_NODE)
    }
}