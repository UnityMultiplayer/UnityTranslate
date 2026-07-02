package xyz.bluspring.unitytranslate.client.gui.screen.config

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfigHolder
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen

class ConfigureTranscriptBoxScreen(val holder: TranscriptBoxConfigHolder) : UTScreen() {
    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)
    }
}
