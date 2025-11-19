package xyz.bluspring.unitytranslate.forge

import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.client.event.RegisterClientCommandsEvent
import net.minecraftforge.client.event.RenderGuiEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.commands.UnityTranslateCommands

object UnityTranslateForgeClient {
    val instance = UnityTranslateClient()

    @SubscribeEvent
    fun onClientTick(ev: TickEvent.ClientTickEvent) {
        if (ev.phase == TickEvent.Phase.END) {
            this.instance.clientTick(Minecraft.getInstance())
        }
    }

    @SubscribeEvent
    fun onClientCommands(ev: RegisterClientCommandsEvent) {
        UnityTranslateCommands.register(ev.dispatcher, "unitytranslateclient", true, CommandSourceStack::sendSystemMessage)
    }

    @SubscribeEvent
    fun onHudRender(ev: RenderGuiEvent.Post) {
        this.instance.clientRenderHud(ev.guiGraphics, ev.partialTick)
    }

    @SubscribeEvent
    fun onClientJoin(ev: ClientPlayerNetworkEvent.LoggingIn) {
        this.instance.clientJoinWorld()
    }

    @SubscribeEvent
    fun onClientLeave(ev: ClientPlayerNetworkEvent.LoggingOut) {
        this.instance.clientLeaveWorld()
    }
}