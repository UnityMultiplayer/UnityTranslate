package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.effects.Effect
import gg.essential.universal.UMatrixStack
import java.awt.Color

class RoundedOutlinedBevelEffect(var size: Float, var color: Color) : Effect() {
    override fun beforeDraw(matrixStack: UMatrixStack) {
        UIRoundedRectangle.drawRoundedRectangle(matrixStack, boundComponent.getLeft() - size - 1f, boundComponent.getTop() - size - 1f, boundComponent.getRight() + size + 1f, boundComponent.getBottom() + size + 1f, boundComponent.getRadius() + size + 1f, color)
        UIRoundedRectangle.drawRoundedRectangle(matrixStack, boundComponent.getLeft() - size, boundComponent.getTop() - size, boundComponent.getRight() + size, boundComponent.getBottom() + size, boundComponent.getRadius() + size, this.boundComponent.getColor())
        UIRoundedRectangle.drawRoundedRectangle(matrixStack, boundComponent.getLeft() - size, boundComponent.getTop() - size, boundComponent.getRight(), boundComponent.getBottom(), boundComponent.getRadius() + size, this.boundComponent.getColor().brighter())
        UIRoundedRectangle.drawRoundedRectangle(matrixStack, boundComponent.getLeft(), boundComponent.getTop(), boundComponent.getRight() + size, boundComponent.getBottom() + size, boundComponent.getRadius() + size, this.boundComponent.getColor().darker())
    }
}