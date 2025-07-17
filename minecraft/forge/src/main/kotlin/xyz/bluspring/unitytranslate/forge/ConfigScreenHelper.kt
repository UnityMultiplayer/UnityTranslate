package xyz.bluspring.unitytranslate.forge

//? if forge {
import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
//?}

object ConfigScreenHelper {
    fun registerConfigScreen() {
        //? if forge {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY) {
            BiFunction { mc, screen ->
                UTConfigScreen(screen)
            }
        }
        //?}
    }
}