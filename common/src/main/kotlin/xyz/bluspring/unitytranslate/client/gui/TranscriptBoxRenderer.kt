package xyz.bluspring.unitytranslate.client.gui

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.client.gui.hud.TranscriptBoxContainer
import xyz.bluspring.unitytranslate.client.renderer.ui.UIGraphics

class TranscriptBoxRenderer {
    val containers = mutableListOf<TranscriptBoxContainer>()

    fun submit(graphics: UIGraphics, partialTick: Float) {
        for (container in this.containers) {
            container.submit(graphics, partialTick)
        }
    }

    fun updateConfig(boxes: Collection<TranscriptBoxConfig>) {
        val existing = this.containers

        // Remove boxes that no longer exist
        for (container in existing.filter { boxes.contains(it.config) }) {
            this.containers.remove(container)
        }

        // Add new boxes
        for (config in boxes) {
            if (existing.none { e -> e.config == config }) {
                this.containers.add(TranscriptBoxContainer(UnityTranslateApi.instance.getOrCreateTranscriptHolder(config.languageCode), config))
            }
        }

        // Now update everyone
        for (container in this.containers) {
            container.updateConfig()
        }
    }
}
