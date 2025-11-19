package xyz.bluspring.unitytranslate.forge

import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.server.permission.events.PermissionGatherEvent
import xyz.bluspring.unitytranslate.commands.UnityTranslateCommands

object ForgeEvents {
     fun init() {
         MinecraftForge.EVENT_BUS.register(this)
     }

     @SubscribeEvent
     fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
         event.addNodes(UnityTranslateForge.REQUEST_TRANSLATIONS_NODE)
     }

    @SubscribeEvent
    fun onCommandRegister(ev: RegisterCommandsEvent) {
        UnityTranslateCommands.register(ev.dispatcher, "unitytranslate", false, CommandSourceStack::sendSystemMessage)
    }
}