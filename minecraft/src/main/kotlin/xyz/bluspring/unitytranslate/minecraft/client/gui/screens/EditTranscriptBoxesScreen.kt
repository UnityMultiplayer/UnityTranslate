package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class EditTranscriptBoxesScreen(val parent: Screen?) : Screen(MinecraftProxy.literal("")) {
    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        UnityTranslateMCClient.transcriptRenderer.render(poseStack, partialTick)

        super.render(poseStack, mouseX, mouseY, partialTick)
    }

    override fun onClose() {
        UnityTranslateMCClient.instance.saveConfig()
        UnityTranslateMCClient.instance.updateConfig()

        Minecraft.getInstance().setScreen(parent)
    }
}