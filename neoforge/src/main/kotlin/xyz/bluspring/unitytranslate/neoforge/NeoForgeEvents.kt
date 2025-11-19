package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent

import xyz.bluspring.unitytranslate.UnityTranslate

object NeoForgeEvents {
     fun init() {
         NeoForge.EVENT_BUS.register(this)
     }

     @SubscribeEvent
     fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
         UnityTranslate.instance.proxy.registerPermissions(event)
     }
}