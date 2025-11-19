package xyz.bluspring.unitytranslate.fabric

import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.UnityTranslate
import java.nio.file.Path

class FabricPlatformProxy : PlatformProxy {
    val loader: FabricLoader = FabricLoader.getInstance()
    val container: ModContainer = loader.getModContainer(UnityTranslate.MOD_ID).orElseThrow()

    override fun isModLoaded(id: String): Boolean {
        return this.loader.isModLoaded(id)
    }

    override fun isClient(): Boolean {
        return this.loader.environmentType == EnvType.CLIENT
    }

    override val modVersion: String
        get() = this.container.metadata.version.friendlyString

    override val configDir: Path
        get() = this.loader.configDir

    override val gameDir: Path
        get() = this.loader.gameDir

    override val isDev: Boolean
        get() = this.loader.isDevelopmentEnvironment

    override fun hasTranscriptPermission(player: Player): Boolean {
        return Permissions.check(player, "${UnityTranslate.MOD_ID}.request_translations")
    }
}