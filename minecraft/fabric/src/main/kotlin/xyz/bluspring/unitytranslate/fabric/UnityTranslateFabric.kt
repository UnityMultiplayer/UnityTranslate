package xyz.bluspring.unitytranslate.fabric

//? if fabric {
import net.fabricmc.api.ModInitializer
import xyz.bluspring.unitytranslate.fabric.network.UTServerNetworkHandler
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC

class UnityTranslateFabric : ModInitializer {
    override fun onInitialize() {
        mcInstance = UnityTranslateMC()

        //? if <= 1.20.4 {
        UTServerNetworkHandler.init()
        //?}
    }

    companion object {
        lateinit var mcInstance: UnityTranslateMC
    }
}
//?}