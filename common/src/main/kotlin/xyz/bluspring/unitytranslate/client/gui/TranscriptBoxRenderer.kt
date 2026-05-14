package xyz.bluspring.unitytranslate.client.gui

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import xyz.bluspring.unitytranslate.client.gui.hud.TranscriptBoxContainer

class TranscriptBoxRenderer : UIContainer() {
    init {
        constrain {
            width = 100.percent
            height = 100.percent
        }
    }

    fun updateConfig() {
        for (container in this.childrenOfType<TranscriptBoxContainer>()) {
            container.updateConfig()
        }
    }
}
