package xyz.bluspring.unitytranslate.fabric

import net.fabricmc.api.ModInitializer
import xyz.bluspring.unitytranslate.UnityTranslate

class UnityTranslateFabric : ModInitializer {
    override fun onInitialize() {
        instance = UnityTranslate()
    }

    companion object {
        lateinit var instance: UnityTranslate
    }
}
