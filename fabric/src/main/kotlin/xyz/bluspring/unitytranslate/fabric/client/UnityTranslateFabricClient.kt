package xyz.bluspring.unitytranslate.fabric.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.client.renderer.UnityTranslateGui
import xyz.bluspring.unitytranslate.client.renderer.ui.MinecraftUIGraphics

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        UnityTranslateClient.init()

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, UnityTranslate.id("transcript_boxes")) { graphics, deltaTracker ->
            UnityTranslateGui.submit(MinecraftUIGraphics(graphics), deltaTracker.getGameTimeDeltaPartialTick(true))
        }
    }
}
