package xyz.bluspring.unitytranslate.gui.visual.transcript

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.childOf
import gg.essential.universal.UMatrixStack
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui

class TranscriptBoxRenderer {
    val window = Window(ElementaVersion.V10)
    val renderedBoxes = mutableListOf<TranscriptBox>()

    fun update() {
        val boxes = UnityTranslateGui.clientConfig.transcriptBoxes

        // Remove all boxes that are no longer listed
        val removed = renderedBoxes.filter { !boxes.contains(it.config) }

        for (box in removed) {
            window.removeChild(box)
        }

        renderedBoxes.removeAll(removed)

        // Then add new boxes accordingly
        val toAdd = boxes.filter { renderedBoxes.none { b -> b.config == it } }
        for (boxConfig in toAdd) {
            val holder = UnityTranslateGui.transcriptHolders[boxConfig.language] ?: continue
            val transcriptBox = TranscriptBox(holder, boxConfig)
            transcriptBox childOf window

            renderedBoxes.add(transcriptBox)
        }

        // Now, update all boxes
        for (box in renderedBoxes) {
            box.update()
        }
    }

    fun render(matrixStack: UMatrixStack, tickDelta: Float) {
        for (box in renderedBoxes) {
            box.tick()
        }

        window.draw(matrixStack)
    }
}