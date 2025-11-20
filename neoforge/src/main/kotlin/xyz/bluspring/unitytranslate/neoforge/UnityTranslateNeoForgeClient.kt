package xyz.bluspring.unitytranslate.neoforge

import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandSourceStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent
import net.neoforged.neoforge.client.event.RenderGuiEvent
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.commands.UnityTranslateCommands

object UnityTranslateNeoForgeClient {
    val instance = UnityTranslateClient()

    @SubscribeEvent
    fun onClientTick(ev: ClientTickEvent.Post) {
        this.instance.clientTick(Minecraft.getInstance())
    }

    @SubscribeEvent
    fun onClientCommands(ev: RegisterClientCommandsEvent) {
        UnityTranslateCommands.register(ev.dispatcher, "unitytranslateclient", true, CommandSourceStack::sendSystemMessage)
    }

    @SubscribeEvent
    fun onHudRender(ev: RenderGuiEvent.Post) {
        this.instance.clientRenderHud(ev.guiGraphics, ev.partialTick.getGameTimeDeltaPartialTick(true))
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