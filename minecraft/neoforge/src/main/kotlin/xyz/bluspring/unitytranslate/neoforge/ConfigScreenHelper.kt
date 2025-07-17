package xyz.bluspring.unitytranslate.neoforge

import net.neoforged.fml.ModLoadingContext
import net.neoforged.neoforge.client.ConfigScreenHandler
import xyz.bluspring.unitytranslate.minecraft.client.gui.UTConfigScreen
import java.util.function.BiFunction

object ConfigScreenHelper {
    fun registerConfigScreen() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { mc, screen ->
                UTConfigScreen(screen)
            }
        }
    }
}