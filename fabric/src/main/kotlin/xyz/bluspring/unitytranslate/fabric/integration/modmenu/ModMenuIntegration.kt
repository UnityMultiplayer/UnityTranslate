package xyz.bluspring.unitytranslate.fabric.integration.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import xyz.bluspring.unitytranslate.client.gui.screen.config.UnityTranslateMCConfigScreen

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            UnityTranslateMCConfigScreen(screen)
        }
    }
}
