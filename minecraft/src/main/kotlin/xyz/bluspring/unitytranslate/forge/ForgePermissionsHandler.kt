package xyz.bluspring.unitytranslate.forge

//#if FORGE
//$$ import net.minecraftforge.fml.common.Mod
//$$ import net.minecraftforge.server.permission.DefaultPermissionLevel
//$$ import net.minecraftforge.server.permission.PermissionAPI
//#endif

//#if FORGE
//$$ @Mod.EventBusSubscriber(modid = UnityTranslate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
//#endif
object ForgePermissionsHandler {
    init {
        //#if FORGE
        //$$ for (permissionNode in Permissions.permissions) {
            //$$ PermissionAPI.registerNode(permissionNode, DefaultPermissionLevel.OP, "")
        //$$ }
        //#endif
    }
}