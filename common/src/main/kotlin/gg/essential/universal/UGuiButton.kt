package gg.essential.universal

import net.minecraft.client.gui.components.AbstractWidget

object UGuiButton {
    @JvmStatic
    fun getX(button: AbstractWidget): Int {
        return button.x
    }

    @JvmStatic
    fun getY(button: AbstractWidget): Int {
        return button.y
    }
}
