package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod.EventBusSubscriber
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.Permissions
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy

@EventBusSubscriber(modid = UnityTranslate.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
object NeoForgePermissionsHandler {
    @JvmStatic
    val permissions = mutableMapOf<String, PermissionNode<Boolean>>()

    @JvmStatic
    @SubscribeEvent
    fun registerPermissions(event: PermissionGatherEvent.Nodes) {
        for (node in Permissions.permissions) {
            event.addNodes(PermissionNode(MinecraftProxy.id(node), PermissionTypes.BOOLEAN, { _, _, _ -> true }).apply {
                permissions[node] = this
            })
        }
    }
}