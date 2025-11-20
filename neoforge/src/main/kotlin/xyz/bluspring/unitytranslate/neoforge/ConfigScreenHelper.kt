package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.fml.ModLoadingContext

import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen
//? if >= 1.20.6 {
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
//?} else {
/*import net.neoforged.neoforge.client.ConfigScreenHandler
*///?}

object ConfigScreenHelper {
    fun createConfigScreen() {
        //? if >= 1.20.6 {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
            IConfigScreenFactory { mc, prev ->
        //?} else {
        /*ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { mc, prev ->
        *///?}
                UTConfigScreen(prev)
            }
        }
    }
}