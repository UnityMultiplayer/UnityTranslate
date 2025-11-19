package xyz.bluspring.unitytranslate.fabric.client

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        val instance = UnityTranslateClient()

        for (key in UnityTranslateClient.keys) {
            KeyBindingHelper.registerKeyBinding(key)
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            instance.clientTick(client)
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            instance.clientStopping()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            instance.clientJoinWorld()
        }

        HudRenderCallback.EVENT.register { guiGraphics, delta ->
            instance.clientRenderHud(guiGraphics, delta)
        }
    }
}
