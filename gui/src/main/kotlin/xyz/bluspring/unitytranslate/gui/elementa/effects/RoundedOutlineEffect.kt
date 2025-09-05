package xyz.bluspring.unitytranslate.gui.elementa.effects

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.effects.Effect
import gg.essential.universal.UMatrixStack
import java.awt.Color

class RoundedOutlineEffect(var size: Float, var color: Color) : Effect() {
    override fun beforeDraw(matrixStack: UMatrixStack) {
        UIRoundedRectangle.drawRoundedRectangle(matrixStack, boundComponent.getLeft() - size, boundComponent.getTop() - size, boundComponent.getRight() + size, boundComponent.getBottom() + size, boundComponent.getRadius() + size, color)
    }
}