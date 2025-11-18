package xyz.bluspring.unitytranslate

//? if fabric {
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
//? }
import net.minecraft.world.entity.player.Player
import java.nio.file.Path
//? if forge {
/*import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.loading.FMLPaths
import net.minecraftforge.server.permission.PermissionAPI
import net.minecraftforge.server.permission.events.PermissionGatherEvent
import net.minecraftforge.server.permission.nodes.PermissionNode
import net.minecraftforge.server.permission.nodes.PermissionTypes
*///? } else if neoforge {
/*import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.ModList
import net.neoforged.fml.loading.FMLLoader
import net.neoforged.fml.loading.FMLPaths
import net.neoforged.neoforge.server.permission.PermissionAPI
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes
*///? }

class PlatformProxyImpl : PlatformProxy {
    override val isDev: Boolean
        //? if fabric {
        get() = FabricLoader.getInstance().isDevelopmentEnvironment
        //? } else if forge_like {
        /*get() = !FMLLoader.isProduction()
        *///? }

    override val gameDir: Path
        //? if fabric {
        get() = FabricLoader.getInstance().gameDir
        //? } else if forge_like {
        /*get() = FMLPaths.GAMEDIR.get()
        *///? }

    override val configDir: Path
        //? if fabric {
        get() = FabricLoader.getInstance().configDir
        //? } else if forge_like {
        /*get() = FMLPaths.CONFIGDIR.get()
        *///? }

    override val modVersion: String
        //? if fabric {
        get() = FabricLoader.getInstance().getModContainer(UnityTranslate.MOD_ID).orElseThrow().metadata.version.friendlyString
        //? } else if forge_like {
        /*get() = ModList.get().getModFileById(UnityTranslate.MOD_ID).versionString()
        *///? }

    override fun isModLoaded(id: String): Boolean {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded(id)
        //? } else if forge_like {
        /*return ModList.get().isLoaded(id)
        *///? }
    }

    override fun isClient(): Boolean {
        //? if fabric {
        return FabricLoader.getInstance().environmentType == EnvType.CLIENT
        //? } else if forge_like {
        /*return FMLLoader.getDist() == Dist.CLIENT
        *///? }
    }

    // how did Forge manage to overcomplicate permissions of all things

    //? if forge_like {
     /*val requestTranslationsNode = PermissionNode(UnityTranslate.MOD_ID, "request_translations", PermissionTypes.BOOLEAN, { _, _, _ -> true })
     override fun registerPermissions(event: PermissionGatherEvent.Nodes) {
        event.addNodes(requestTranslationsNode)
     }
    *///? }

    override fun hasTranscriptPermission(player: Player): Boolean {
        //? if fabric {
        return Permissions.check(player, "unitytranslate.request_translations", true)
        //? } else if forge_like {
        /*return PermissionAPI.getOfflinePermission(player.uuid, requestTranslationsNode)
        *///? }
    }
}