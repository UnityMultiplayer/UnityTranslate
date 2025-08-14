package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.effects.Effect
import gg.essential.universal.UMatrixStack
import java.awt.Color

class ProgressEffect(var color: Color, var progress: Double = 0.0) : Effect() {
    override fun afterDraw(matrixStack: UMatrixStack) {
        UIBlock.drawBlock(matrixStack, color,
            boundComponent.getLeft().toDouble(), boundComponent.getTop().toDouble(),
            boundComponent.getLeft().toDouble() + (boundComponent.getWidth().toDouble() * progress), boundComponent.getBottom().toDouble()
        )
    }
}