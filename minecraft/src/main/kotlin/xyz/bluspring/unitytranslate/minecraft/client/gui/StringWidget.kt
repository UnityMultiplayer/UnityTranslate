package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

class StringWidget(x: Int, y: Int, message: Component, protected val font: Font) : AbstractWidget(x, y, font.width(message.visualOrderText), 9, message) {
    var alignX = 0.5f
    protected var color: Int = 0xFFFFFF
    var tooltip: Component? = null

    override fun updateNarration(output: NarrationElementOutput) {
    }

    fun alignLeft(): StringWidget {
        this.alignX = 0f
        return this
    }

    fun alignCenter(): StringWidget {
        this.alignX = 0.5f
        return this
    }

    fun alignRight(): StringWidget {
        this.alignX = 1f
        return this
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        val x = this.x + (this.alignX * (this.width - this.font.width(this.message)).toFloat()).roundToInt()
        val y = this.y + (this.height - 9) / 2

        //#if MC < 1.20.1
        drawString(poseStack, font, message, x, y, color)

        if (this.isHoveredOrFocused && tooltip != null) {
            Minecraft.getInstance().screen?.renderTooltip(poseStack, tooltip!!, mouseX, mouseY)
        }
        //#endif
    }
}