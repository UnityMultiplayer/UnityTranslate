package xyz.bluspring.unitytranslate.client.renderer.ui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.FormattedCharSequence

interface UIGraphics {
    val poseStack: PoseStack
    val width: Int
    val height: Int
    fun enableScissor(x: Int, y: Int, width: Int, height: Int)
    fun disableScissor()

    fun drawCenteredString(font: Font, text: Component, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text.visualOrderText, x - (font.width(text) / 2f), y, color, dropShadow)

    fun drawCenteredString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text, x - (font.width(text) / 2f), y, color, dropShadow)

    fun drawString(font: Font, text: Component, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text.visualOrderText, x, y, color, dropShadow)

    fun drawString(font: Font, text: FormattedText, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, Language.getInstance().getVisualOrder(text), x, y, color, dropShadow)

    fun drawString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean)

    fun fill(x1: Float, y1: Float, x2: Float, y2: Float, colorFrom: Int, colorTo: Int = colorFrom)
        = fill(x1, y1, x2, y2, colorFrom, colorFrom, colorTo, colorTo)

    fun fill(x1: Float, y1: Float, x2: Float, y2: Float, colorTopLeft: Int, colorTopRight: Int, colorBottomLeft: Int, colorBottomRight: Int)

    fun outline(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, thickness: Float = 1f) {
        // top
        this.fill(x1, y1, x2, y1 + thickness, color)
        this.fill(x1, y2 - thickness, x2, y2, color)

        // sides
        this.fill(x1, y1 + thickness, x1 + thickness, y2 - thickness, color)
        this.fill(x2 - thickness, y1 + thickness, x2, y2 - thickness, color)
    }
}
