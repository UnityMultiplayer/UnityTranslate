package xyz.bluspring.unitytranslate.forge

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.permission.DefaultPermissionLevel
import net.minecraftforge.server.permission.PermissionAPI
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.Permissions

@Mod.EventBusSubscriber(modid = UnityTranslate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ForgePermissionsHandler {
    init {
        for (permissionNode in Permissions.permissions) {
            PermissionAPI.registerNode(permissionNode, DefaultPermissionLevel.OP, "")
        }
    }
}