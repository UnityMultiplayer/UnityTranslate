package xyz.bluspring.unitytranslate.forge

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.server.permission.events.PermissionGatherEvent

object ForgeEvents {
     fun init() {
         MinecraftForge.EVENT_BUS.register(this)
     }

     @SubscribeEvent
     fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
         event.addNodes(UnityTranslateForge.REQUEST_TRANSLATIONS_NODE)
     }
}