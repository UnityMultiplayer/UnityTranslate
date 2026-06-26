package xyz.bluspring.unitytranslate.client.renderer

import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.client.gui.LogoTransitionOverlay
import xyz.bluspring.unitytranslate.client.gui.TranscriptBoxRenderer

object UnityTranslateGui {
    val transcriptRenderer = TranscriptBoxRenderer()

    init {
        transcriptRenderer.updateConfig(listOf(
            TranscriptBoxConfig("en", TranscriptBoxConfig.Transforms(
                TranscriptBoxConfig.Transforms.Position.Relative(0.1f, 0.1f),
                TranscriptBoxConfig.Transforms.Size.Anchored(320f, 413f),
            ))
        ))
    }

    fun resize() {
        for (container in this.transcriptRenderer.containers) {
            container.updateConfig()
        }
    }

    private var lastWidth = 0
    private var lastHeight = 0
    private var lastGui = 0.0

    private fun updateSizes() {
        if (ClientPlatformProxy.instance.windowWidth != lastWidth || ClientPlatformProxy.instance.windowHeight != lastHeight || ClientPlatformProxy.instance.guiScale != lastGui) {
            this.resize()
            this.lastWidth = ClientPlatformProxy.instance.windowWidth
            this.lastHeight = ClientPlatformProxy.instance.windowHeight
            this.lastGui = ClientPlatformProxy.instance.guiScale
        }
    }

    fun submit(uiGraphics: UIGraphics, partialTick: Float) {
        updateSizes()
        this.transcriptRenderer.submit(uiGraphics, partialTick)
    }

    fun submitLate(graphics: UIGraphics, partialTick: Float) {
        updateSizes()
        LogoTransitionOverlay.submit(graphics, partialTick)
    }
}
