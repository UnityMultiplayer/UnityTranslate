package xyz.bluspring.unitytranslate.forge

import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.client.event.RenderGuiEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

object UnityTranslateForgeClient {
    val instance = UnityTranslateClient()

    @SubscribeEvent
    fun onClientTick(ev: TickEvent.ClientTickEvent) {
        if (ev.phase == TickEvent.Phase.END) {
            this.instance.clientTick(Minecraft.getInstance())
        }
    }

    @SubscribeEvent
    fun onHudRender(ev: RenderGuiEvent.Post) {
        this.instance.clientRenderHud(ev.guiGraphics, ev.partialTick)
    }

    @SubscribeEvent
    fun onClientJoin(ev: ClientPlayerNetworkEvent.LoggingIn) {
        this.instance.clientJoinWorld()
    }
}