package xyz.bluspring.unitytranslate.fabric

import net.fabricmc.api.ModInitializer
import xyz.bluspring.unitytranslate.minecraft.UnityTranslateMC

class UnityTranslateFabric : ModInitializer {
    override fun onInitialize() {
        mcInstance = UnityTranslateMC()
    }

    companion object {
        lateinit var mcInstance: UnityTranslateMC
    }
}