package xyz.bluspring.unitytranslate.forge

import net.minecraftforge.fml.ExtensionPoint
import net.minecraftforge.fml.ModLoadingContext
import xyz.bluspring.unitytranslate.minecraft.client.gui.UTConfigScreen
import java.util.function.BiFunction

object ConfigScreenHelper {
    fun registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY) {
            BiFunction { mc, screen ->
                UTConfigScreen(screen)
            }
        }
    }
}