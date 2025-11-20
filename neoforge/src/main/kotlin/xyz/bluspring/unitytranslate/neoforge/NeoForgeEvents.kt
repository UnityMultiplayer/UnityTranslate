package xyz.bluspring.unitytranslate.neoforge

import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent

import xyz.bluspring.unitytranslate.commands.UnityTranslateCommands
import xyz.bluspring.unitytranslate.network.UTServerNetworking
import xyz.bluspring.unitytranslate.translator.TranslatorManager

object NeoForgeEvents {
     fun init() {
         NeoForge.EVENT_BUS.register(this)
     }

    @SubscribeEvent
    fun onPermissionsGather(event: PermissionGatherEvent.Nodes) {
        event.addNodes(UnityTranslateNeoForge.REQUEST_TRANSLATIONS_NODE)
    }

    @SubscribeEvent
    fun onCommandRegister(ev: RegisterCommandsEvent) {
        UnityTranslateCommands.register(ev.dispatcher, "unitytranslate", false, CommandSourceStack::sendSystemMessage)
    }

    @SubscribeEvent
    fun onPlayerJoin(ev: PlayerEvent.PlayerLoggedInEvent) {
        if (ev.entity !is ServerPlayer)
            return

        UTServerNetworking.onPlayerJoin(ev.entity as ServerPlayer)
    }

    @SubscribeEvent
    fun onPlayerLeave(ev: PlayerEvent.PlayerLoggedOutEvent) {
        if (ev.entity !is ServerPlayer)
            return

        TranslatorManager.playerQuit(ev.entity)
        UTServerNetworking.onPlayerLeave(ev.entity as ServerPlayer)
    }
}