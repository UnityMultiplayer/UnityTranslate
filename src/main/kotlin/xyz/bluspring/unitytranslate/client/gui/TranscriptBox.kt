package xyz.bluspring.unitytranslate.client.gui

import kotlinx.serialization.Serializable
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
//? if < 1.21.4 {
import net.minecraft.util.FastColor.ARGB32 as ARGB
//?} else {
/*import net.minecraft.util.ARGB
*///?}
import net.minecraft.util.Mth
//? if >= 1.21.8 {
/*import org.joml.Matrix3x2fStack
*///?}
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.util.multiversion.*
import java.util.*

@Serializable
data class TranscriptBox(
    var offsetX: Int,
    var offsetY: Int,
    var width: Int,
    var height: Int,
    var opacity: Int,

    var language: Language,

    var offsetXEdge: Boolean = false,
    var offsetYEdge: Boolean = false
) {
    var x: Int
        get() {
            return if (offsetXEdge)
                Minecraft.getInstance().window.guiScaledWidth - offsetX
            else
                offsetX
        }
        set(value) {
            if (value > Minecraft.getInstance().window.guiScaledWidth / 2 - (width / 2)) {
                offsetXEdge = true
                offsetX = Minecraft.getInstance().window.guiScaledWidth - value
            } else {
                offsetXEdge = false
                offsetX = value
            }
        }

    var y: Int
        get() {
            return if (offsetYEdge)
                Minecraft.getInstance().window.guiScaledHeight - offsetY
            else
                offsetY
        }
        set(value) {
            if (value > Minecraft.getInstance().window.guiScaledHeight / 2 - (height / 2)) {
                offsetYEdge = true
                offsetY = Minecraft.getInstance().window.guiScaledHeight - value
            } else {
                offsetYEdge = false
                offsetY = value
            }
        }

    //? if >= 1.21.8 {
    /*fun Matrix3x2fStack.pushPose() {
        this.pushMatrix()
    }

    fun Matrix3x2fStack.translate(x: Double, y: Double, z: Double) {
        this.translate(x.toFloat(), y.toFloat())
    }

    fun Matrix3x2fStack.translate(x: Float, y: Float, z: Float) {
        this.translate(x, y)
    }

    fun Matrix3x2fStack.scale(x: Float, y: Float, z: Float) {
        this.scale(x, y)
    }

    fun Matrix3x2fStack.popPose() {
        this.popMatrix()
    }
    *///?}

    fun render(guiGraphics: GuiGraphics, partialTick: Float) {
        val holder = UnityTranslateClient.getTranscriptHolder(this.language) ?: return

        guiGraphics.pose().pushPose()

        guiGraphics.pose().translate(0.0, 0.0, -255.0)
        guiGraphics.enableScissor(x, y, x + width, y + height)

        guiGraphics.fill(x, y, x + width, y + height, ARGB.color(opacity, 0, 0, 0))
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable("unitytranslate.transcript").append(" (${language.code.uppercase()})")
            .withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD), x + (width / 2), y + 5, 16777215)

        if (!UnityTranslateClient.shouldTranscribe) {
            guiGraphics.blitTexture(TRANSCRIPT_MUTED, x + width - 20, y + 2, 0f, 0f, 16, 16, 16, 16)
        }

        guiGraphics.enableScissor(x, y + 15, x + width, y + height)

        val lines = holder.transcripts.sortedByDescending { it.arrivalTime }

        val font = Minecraft.getInstance().font
        val scale = UnityTranslate.config.client.textScale / 100f
        val invScale = if (scale == 0f) 0f else 1f / scale

        var currentY = y + height - font.lineHeight

        for (transcript in lines) {
            val component = Component.empty()
                .append("<")
                .append(transcript.player.displayName)
                .append(
                    Component.literal(" (${transcript.language.code.uppercase(Locale.ENGLISH)})")
                        .withStyle(ChatFormatting.GREEN)
                )
                .append("> ")
                .append(
                    Component.literal(transcript.text)
                        .apply {
                            if (transcript.incomplete) {
                                this.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                            }
                        }
                )

            val currentTime = System.currentTimeMillis()
            val delay = (UnityTranslate.config.client.disappearingTextDelay * 1000L).toLong()

            var alpha = 1f
            if (UnityTranslate.config.client.disappearingText && currentTime >= transcript.arrivalTime + delay) {
                val fadeTime = (UnityTranslate.config.client.disappearingTextFade * 1000L).toLong()

                val fadeStart = transcript.arrivalTime + delay
                val fadeEnd = fadeStart + fadeTime
                val fadeAmount = ((fadeEnd - currentTime).toFloat() / fadeTime.toFloat())

                alpha = Mth.clamp(fadeAmount, 0f, 1f)
            }

            val split = font.split(component, ((width - 5) * invScale).toInt()).reversed()

            //? if < 1.21.4 {
            guiGraphics.setColor(1f, 1f, 1f, alpha)
            //?}

            for (line in split) {
                guiGraphics.pose().pushPose()
                guiGraphics.pose().translate(x.toFloat(), currentY.toFloat(), 0f)
                guiGraphics.pose().scale(scale, scale, scale)
                guiGraphics.drawString(font, line, 4, 0, ARGB.color((alpha * 255f).toInt(), 255, 255, 255))
                currentY -= (font.lineHeight * scale).toInt()
                guiGraphics.pose().popPose()
            }

            //? if < 1.21.4 {
            guiGraphics.setColor(1f, 1f, 1f, 1f)
            //?}

            currentY -= 4
        }

        guiGraphics.disableScissor()
        guiGraphics.disableScissor()

        guiGraphics.renderOutline(x, y, width, height, ARGB.color(100, 0, 0, 0))

        guiGraphics.pose().popPose()
    }

    companion object {
        val TRANSCRIPT_MUTED = UnityTranslate.id("textures/gui/transcription_muted.png")
    }
}