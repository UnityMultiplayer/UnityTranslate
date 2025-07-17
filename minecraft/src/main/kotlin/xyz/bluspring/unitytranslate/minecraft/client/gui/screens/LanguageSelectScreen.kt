package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import xyz.bluspring.unitytranslate.common.Language

class LanguageSelectScreen(val parent: Screen? = null, val onSelected: (Language?) -> Unit, val hasDefault: Boolean = false) : WindowScreen(ElementaVersion.V10) {
    init {

    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }
}