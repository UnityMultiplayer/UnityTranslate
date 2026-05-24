package xyz.bluspring.unitytranslate.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.client.gui.hud.TranscriptBoxContainer

class TranscriptBoxRenderer {
    val containers = mutableListOf<TranscriptBoxContainer>()

    fun submit(poseStack: PoseStack, partialTick: Float) {
        for ((index, container) in this.containers.withIndex()) {
            poseStack.pushPose()
            poseStack.translate(0f, 0f, index * 100f)
            container.submit(poseStack, partialTick)
            poseStack.popPose()
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
