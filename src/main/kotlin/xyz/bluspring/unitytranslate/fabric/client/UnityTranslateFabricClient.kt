package xyz.bluspring.unitytranslate.fabric.client

//? if fabric {
import net.fabricmc.api.ClientModInitializer
import xyz.bluspring.unitytranslate.client.UnityTranslateClient

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        UnityTranslateClient()
        UnityTranslateClient.registerKeys()
    }
}
//? }