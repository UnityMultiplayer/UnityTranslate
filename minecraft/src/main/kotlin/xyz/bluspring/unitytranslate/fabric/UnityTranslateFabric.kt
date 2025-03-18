package xyz.bluspring.unitytranslate.fabric

//#if FABRIC
import net.fabricmc.api.ModInitializer
import xyz.bluspring.unitytranslate.fabric.network.UTServerNetworkHandler
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC

class UnityTranslateFabric : ModInitializer {
    override fun onInitialize() {
        mcInstance = UnityTranslateMC()

        //#if MC <= 1.20.4
        UTServerNetworkHandler.init()
        //#endif
    }

    companion object {
        lateinit var mcInstance: UnityTranslateMC
    }
}
//#endif