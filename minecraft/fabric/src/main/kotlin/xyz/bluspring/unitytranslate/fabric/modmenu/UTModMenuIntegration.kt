package xyz.bluspring.unitytranslate.fabric.modmenu

//? if fabric {
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import xyz.bluspring.unitytranslate.minecraft.client.gui.UTConfigScreen

class UTModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            UTConfigScreen(parent)
        }
    }
}
//?}