package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.unitytranslate.client.gui.GuiColors
import xyz.bluspring.unitytranslate.client.renderer.UIGraphics

abstract class ConfigEntry<E, T : ConfigValue<E>>(val value: T) {
    var isFocused = false

    open fun submit(graphics: UIGraphics, partialTick: Float, areaWidth: Int, mouseX: Int, mouseY: Int) {
        val font = Minecraft.getInstance().font
        graphics.drawString(font, Component.translatable("unitytranslate.config.${value.fullId}"), 0f, 0f, if (isFocused) GuiColors.TEXT_FOCUSED else GuiColors.TEXT_UNFOCUSED, true)
    }
}
