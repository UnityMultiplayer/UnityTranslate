package xyz.bluspring.unitytranslate.neoforge

//? if forge_like {

//? if forge {
/*import net.minecraftforge.fml.ModLoadingContext
*///? } else if neoforge {
/*import net.neoforged.fml.ModLoadingContext
*///? }

//? if >= 1.20.6 {
/*import net.neoforged.neoforge.client.gui.IConfigScreenFactory
*///? } else {
    //? if forge {
    /*import net.minecraftforge.client.ConfigScreenHandler
    *///? } else {
    /*import net.neoforged.neoforge.client.ConfigScreenHandler
    *///? }
//? }

//? }

object ConfigScreenHelper {
    fun createConfigScreen() {
//? if forge_like {
    /*//? if >= 1.20.6 {
        /^ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory::class.java) {
            IConfigScreenFactory { mc, prev ->
        ^///? } else {
        /^ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { mc, prev ->
        ^///? }
                UTConfigScreen(prev)
            }
        }
    *///? }
    }
}