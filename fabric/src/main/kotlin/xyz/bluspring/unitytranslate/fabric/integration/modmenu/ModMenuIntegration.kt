package xyz.bluspring.unitytranslate.fabric.integration.modmenu

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import xyz.bluspring.unitytranslate.client.gui.WrappedUTScreen
import xyz.bluspring.unitytranslate.client.gui.screen.config.UnityTranslateConfigScreen

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { screen ->
            WrappedUTScreen(UnityTranslateConfigScreen(), screen)
        }
    }
}
