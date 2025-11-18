package xyz.bluspring.unitytranslate.neoforge

//? if neoforge {
/*import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
*///? } else if forge {
/*import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.server.permission.events.PermissionGatherEvent
*///? }

//? if forge_like {
/*import xyz.bluspring.unitytranslate.UnityTranslate

 object NeoForgeEvents {
     fun init() {
            //? if forge {
             MinecraftForge.EVENT_BUS.register(this)
            //? } else if neoforge {
             NeoForge.EVENT_BUS.register(this)
            //? }
     }

     @SubscribeEvent
     fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
         UnityTranslate.instance.proxy.registerPermissions(event)
     }
}
*///? }