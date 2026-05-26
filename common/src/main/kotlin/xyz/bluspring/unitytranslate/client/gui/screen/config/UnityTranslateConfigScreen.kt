package xyz.bluspring.unitytranslate.client.gui.screen.config

import com.mojang.blaze3d.vertex.PoseStack
import xyz.bluspring.sunset.SunsetConfig

class UnityTranslateConfigScreen(val config: SunsetConfig) {
    fun submit(poseStack: PoseStack, partialTick: Float) {
        for (category in this.config.rootCategory.value) {

        }
    }
}
