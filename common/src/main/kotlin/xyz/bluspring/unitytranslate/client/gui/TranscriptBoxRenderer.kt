package xyz.bluspring.unitytranslate.client.gui

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.client.gui.hud.TranscriptBoxContainer

class TranscriptBoxRenderer : UIContainer() {
    init {
        constrain {
            width = 100.percent
            height = 100.percent
        }
    }

    fun updateConfig(boxes: Collection<TranscriptBoxConfig>) {
        val existing = this.childrenOfType<TranscriptBoxContainer>()

        // Remove boxes that no longer exist
        for (container in existing.filter { boxes.contains(it.config) }) {
            this.removeChild(container)
        }

        // Add new boxes
        for (config in boxes) {
            if (existing.none { e -> e.config == config }) {
                TranscriptBoxContainer(
                    UnityTranslateApi.instance.getOrCreateTranscriptHolder(config.languageCode), config
                ) childOf this
            }
        }

        // Now update everyone
        for (container in this.childrenOfType<TranscriptBoxContainer>()) {
            container.updateConfig()
        }
    }
}
