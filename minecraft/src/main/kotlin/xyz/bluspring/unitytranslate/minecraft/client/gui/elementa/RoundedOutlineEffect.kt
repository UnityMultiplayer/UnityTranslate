package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.effects.Effect
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.vertex.UBufferBuilder
import java.awt.Color

class RoundedOutlineEffect(var size: Float, var color: Color) : Effect() {
    override fun beforeDraw(matrixStack: UMatrixStack) {
        UIRoundedRectangle.drawRoundedRectangle(matrixStack, boundComponent.getLeft() - size, boundComponent.getTop() - size, boundComponent.getRight() + size, boundComponent.getBottom() + size, boundComponent.getRadius() + size, color)
    }
}