package xyz.bluspring.unitytranslate.fabric.client

//#if FABRIC
import net.fabricmc.api.ClientModInitializer
import xyz.bluspring.unitytranslate.fabric.network.UTClientNetworkHandler
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class UnityTranslateFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        mcClientInstance = UnityTranslateMCClient()

        //#if MC <= 1.20.4
        UTClientNetworkHandler.init()
        //#endif
    }

    companion object {
        lateinit var mcClientInstance: UnityTranslateMCClient
    }
}
//#endif