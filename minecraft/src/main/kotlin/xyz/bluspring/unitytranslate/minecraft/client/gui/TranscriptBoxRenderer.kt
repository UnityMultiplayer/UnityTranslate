package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FastColor
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import java.util.*

class TranscriptBoxRenderer {
    val screenWidth: Int
        get() = Minecraft.getInstance().window.guiScaledWidth
    val screenHeight: Int
        get() = Minecraft.getInstance().window.guiScaledHeight

    fun render(
        poseStack: PoseStack
    ) {
        val boxes = UnityTranslateMCClient.clientConfig.transcriptBoxes

        for (box in boxes) {
            renderBox(box, poseStack)
        }
    }

    fun renderBox(box: UnityTranslateClientConfig.TranscriptBoxConfig,
                  poseStack: PoseStack
    ) {
        val font = Minecraft.getInstance().font
        val scale = box.textScale / 100f
        val invScale = if (scale == 0f) 0f else 1f / scale
        val holder = UnityTranslateMCClient.transcriptHolders[box.language] ?: return

        val widthDiv = screenWidth / 3
        val heightDiv = screenHeight / 3

        val minX = when (box.horizontalAlignType) {
            UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE -> (box.offsetX * widthDiv)
            UnityTranslateClientConfig.HorizontalAlignType.CENTER -> (box.offsetX * widthDiv) + (screenWidth / 2)
            UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE -> screenWidth - (box.offsetX * widthDiv)
        }.toInt()

        val minY = when (box.verticalAlignType) {
            UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE -> box.offsetY * heightDiv
            UnityTranslateClientConfig.VerticalAlignType.CENTER -> (box.offsetY * heightDiv) + (screenHeight / 2)
            UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE -> screenHeight - (box.offsetY * heightDiv)
        }.toInt()

        val width = box.width
        val height = box.height

        val maxX = minX + width
        val maxY = minY + height

        val (r, g, b) = box.color.unpackRGB()

        //RenderSystem.enableScissor(minX, minY, width, height)
        poseStack.pushPose()
        poseStack.translate(0.0, 0.0, 250.0)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.enableTexture()
        if (box.textureLocation != null) {
            val textureId = ResourceLocation.tryParse(box.textureLocation!!)!!
            val texture = Minecraft.getInstance().textureManager.getTexture(textureId)

            RenderSystem.setShaderTexture(0, texture.id)
            Screen.blit(poseStack, minX, minY, width, height, 0f, 0f, width, height, width, height)
        } else {
            Screen.fill(poseStack, minX, minY, maxX, maxY, FastColor.ARGB32.color(box.opacity, r, g, b))
        }

        RenderSystem.disableTexture()
        RenderSystem.disableBlend()

        if (box.headerType != UnityTranslateClientConfig.HeaderType.NONE) {
            val text = when (box.headerType) {
                UnityTranslateClientConfig.HeaderType.SHORT_LANG -> MinecraftProxy.translatable("unitytranslate.transcript").append(" (${box.language.code.uppercase()})")
                UnityTranslateClientConfig.HeaderType.LONG_LANG -> MinecraftProxy.translatable("unitytranslate.transcript")
                    .append(" (").append(MinecraftProxy.translatable(box.language.translationKey)).append(")")
                else -> MinecraftProxy.literal("")
            }

            Screen.drawCenteredString(poseStack, font, text, minX + (width / 2), minY + 5, 16777215)
            //RenderSystem.enableScissor(minX, minY + 15, width, height)
        }

        var currentY = minY + height - font.lineHeight

        val currentTime = System.currentTimeMillis()
        val delay = (UnityTranslateMCClient.clientConfig.disappearingTextDelay * 1000L).toLong()

        for (transcript in holder.transcripts.reversed()) {
            val component = MinecraftProxy.translatable("chat.type.text", (transcript.playerRef as Player).displayName
                    .copy()
                    .append(MinecraftProxy.literal(" (${transcript.language.code.uppercase(Locale.ENGLISH)})").withStyle(ChatFormatting.GREEN)),
                MinecraftProxy.literal(transcript.text).apply {
                    if (transcript.incomplete)
                        this.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                }
            )

            if (UnityTranslateMCClient.clientConfig.disappearingText && currentTime >= transcript.arrivalTime + delay) {
                val fadeTime = (UnityTranslateMCClient.clientConfig.disappearingTextFade * 1000L).toLong()

                val fadeStart = transcript.arrivalTime + delay
                val fadeEnd = fadeStart + fadeTime
                val fadeAmount = ((fadeEnd - currentTime).toFloat() / fadeTime.toFloat())

                val alpha = Mth.clamp(fadeAmount, 0f, 1f)
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha)
            }

            val split = font.split(component, ((width - 5) * invScale).toInt()).reversed()

            for (line in split) {
                poseStack.pushPose()
                poseStack.translate(minX.toDouble(), currentY.toDouble(), 0.0)
                poseStack.scale(scale, scale, scale)
                Screen.drawString(poseStack, font, line, 4, 0, FastColor.ARGB32.color(255, 255, 255, 255))
                currentY -= (font.lineHeight * scale).toInt()
                poseStack.popPose()
            }

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
            currentY -= 4
        }

        poseStack.popPose()
        RenderSystem.disableScissor()
    }

    fun Int.unpackRGB(): Triple<Int, Int, Int> {
        // FF_FF_FF
        // 11111111_11111111_11111111
        return Triple(
            (this shr 16) and 0xFF,
            (this shr 8) and 0xFF,
            this and 0xFF
        )
    }
}