package xyz.bluspring.unitytranslate.fabric.client

import gg.essential.universal.UMatrixStack
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.renderer.UnityTranslateElementaGui

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, UnityTranslate.id("transcript_boxes")) { graphics, deltaTracker ->
            UnityTranslateElementaGui.render(UMatrixStack(graphics.pose()), (ClientPlatformProxy.instance.mouseX / ClientPlatformProxy.instance.guiScale).toInt(), (ClientPlatformProxy.instance.mouseY / ClientPlatformProxy.instance.guiScale).toInt(), deltaTracker.getGameTimeDeltaPartialTick(true))
        }
    }
}
