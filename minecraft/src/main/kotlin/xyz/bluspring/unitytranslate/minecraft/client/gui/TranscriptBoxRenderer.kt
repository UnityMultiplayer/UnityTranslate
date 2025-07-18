package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.childOf
import gg.essential.universal.UMatrixStack
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class TranscriptBoxRenderer {
    val window = Window(ElementaVersion.V10)
    val renderedBoxes = mutableListOf<TranscriptBox>()

    fun update() {
        val boxes = UnityTranslateMCClient.clientConfig.transcriptBoxes

        // Remove all boxes that are no longer listed
        val removed = renderedBoxes.filter { !boxes.contains(it.config) }

        for (box in removed) {
            window.removeChild(box)
        }

        renderedBoxes.removeAll(removed)

        // Then add new boxes accordingly
        val toAdd = boxes.filter { renderedBoxes.none { b -> b.config == it } }
        for (boxConfig in toAdd) {
            val holder = UnityTranslateMCClient.transcriptHolders[boxConfig.language] ?: continue
            val transcriptBox = TranscriptBox(holder, boxConfig)
            transcriptBox childOf window
        }

        // Now, update all boxes
        for (box in renderedBoxes) {
            box.update()
        }
    }

    fun render(poseStack: PoseStack, tickDelta: Float) {
        render(UMatrixStack(poseStack), tickDelta)
    }

    fun render(matrixStack: UMatrixStack, tickDelta: Float) {
        for (box in renderedBoxes) {
            box.tick()
        }

        window.draw(matrixStack)
    }
}