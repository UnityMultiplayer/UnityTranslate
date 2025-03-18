package xyz.bluspring.unitytranslate.minecraft.client.gui

import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.minecraft.duck.ScrollableWidget

class ButtonBuilder(val message: Component, val onPress: Button.OnPress) {
    private var x = 0
    private var y = 0
    private var width = UTConfigScreen.BUTTON_WIDTH
    private var height = UTConfigScreen.BUTTON_HEIGHT
    private var tooltip: Component? = null

    fun pos(x: Int, y: Int): ButtonBuilder {
        this.x = x
        this.y = y
        return this
    }

    fun width(width: Int): ButtonBuilder {
        this.width = width
        return this
    }

    fun size(width: Int, height: Int): ButtonBuilder {
        this.width = width
        this.height = height
        return this
    }

    fun tooltip(text: Component): ButtonBuilder {
        this.tooltip = text
        return this
    }

    fun build(): Button {
        //#if MC < 1.20.1
        return Button(x, y, width, height, message, onPress).apply {
            (this as ScrollableWidget).tooltip = this@ButtonBuilder.tooltip
        }
        //#else
        //$$ throw IllegalStateException()
        //#endif
    }

    companion object {
        fun builder(
            message: Component,
            onPress: Button.OnPress
        //#if MC < 1.20.1
        ): ButtonBuilder {
            return ButtonBuilder(message, onPress)
        //#else
        //$$ ): Button.Builder {
        //$$    return Button.builder(message, onPress)
        //#endif
        }
    }
}