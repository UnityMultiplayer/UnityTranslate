package xyz.bluspring.unitytranslate.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen

// need to make sure we actually delay screen opening a little or do things in a different way, otherwise it blurs twice
fun Minecraft.openScreen(screen: Screen?) {
    //? if >= 1.21.10 {
    /*if (screen == null)
        this.setScreen(null)
    else
        this.setScreenAndShow(screen)
    *///?} else {
    this.execute { this.setScreen(screen) }
    //?}
}