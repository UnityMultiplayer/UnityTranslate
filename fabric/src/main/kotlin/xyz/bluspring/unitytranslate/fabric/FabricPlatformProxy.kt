package xyz.bluspring.unitytranslate.fabric

import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.UnityTranslate
import java.nio.file.Path

class FabricPlatformProxy : PlatformProxy {
    override val version: String = FabricLoader.getInstance().getModContainer(UnityTranslate.MOD_ID).orElseThrow().metadata.version.friendlyString
    override val pluginsDir: Path = FabricLoader.getInstance().gameDir.resolve("unitytranslate/plugins")
    override val nativesDir: Path = FabricLoader.getInstance().gameDir.resolve("unitytranslate/natives")
    override val rootDir: Path = FabricLoader.getInstance().gameDir

    override fun isModLoaded(id: String): Boolean = FabricLoader.getInstance().isModLoaded(id)
}
