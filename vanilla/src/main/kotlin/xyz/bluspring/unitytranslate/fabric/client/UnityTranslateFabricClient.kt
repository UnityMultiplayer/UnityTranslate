package xyz.bluspring.unitytranslate.fabric.client

import net.fabricmc.api.ClientModInitializer
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        mcClientInstance = UnityTranslateMCClient()
    }

    companion object {
        lateinit var mcClientInstance: UnityTranslateMCClient
    }
}