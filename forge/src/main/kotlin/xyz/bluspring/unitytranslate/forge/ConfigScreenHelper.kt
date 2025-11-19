package xyz.bluspring.unitytranslate.forge

import dev.nyon.klf.KotlinModContainer
import net.minecraftforge.client.ConfigScreenHandler
import xyz.bluspring.unitytranslate.client.gui.UTConfigScreen

object ConfigScreenHelper {
    fun createConfigScreen(context: KotlinModContainer) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { mc, prev ->
                UTConfigScreen(prev)
            }
        }
    }
}