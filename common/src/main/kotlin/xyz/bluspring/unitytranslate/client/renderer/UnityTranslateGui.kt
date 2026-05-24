package xyz.bluspring.unitytranslate.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
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

    fun submit(poseStack: PoseStack, partialTick: Float) {
        this.transcriptRenderer.submit(poseStack, partialTick)
    }
}
