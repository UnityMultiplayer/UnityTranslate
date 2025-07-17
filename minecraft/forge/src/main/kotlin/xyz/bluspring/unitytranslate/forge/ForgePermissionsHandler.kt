package xyz.bluspring.unitytranslate.forge

import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.server.permission.PermissionAPI
import net.minecraftforge.server.permission.events.PermissionGatherEvent
import net.minecraftforge.server.permission.nodes.PermissionNode
import net.minecraftforge.server.permission.nodes.PermissionTypes
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.Permissions
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy

@Mod.EventBusSubscriber(modid = UnityTranslate.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ForgePermissionsHandler {
    init {
        /*for (permissionNode in Permissions.permissions) {
            PermissionAPI.registerNode(permissionNode, DefaultPermissionLevel.OP, "")
        }*/
    }

    @JvmStatic
    val permissions = mutableMapOf<String, PermissionNode<Boolean>>()

    @SubscribeEvent
    @JvmStatic
    fun registerPermissions(event: PermissionGatherEvent.Nodes) {
        for (node in Permissions.permissions) {
            event.addNodes(PermissionNode(MinecraftProxy.id(node), PermissionTypes.BOOLEAN, { _, _, _ -> true }).apply {
                permissions[node] = this
            })
        }
    }
}