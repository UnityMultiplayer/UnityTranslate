package xyz.bluspring.unitytranslate.forge

//#if FORGE
//$$ import net.minecraftforge.fml.ExtensionPoint
//$$ import net.minecraftforge.fml.ModLoadingContext
//#endif

object ConfigScreenHelper {
    fun registerConfigScreen() {
        //#if FORGE
        //$$ ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY) {
            //$$ BiFunction { mc, screen ->
                //$$ UTConfigScreen(screen)
            //$$ }
        //$$ }
        //#endif
    }
}