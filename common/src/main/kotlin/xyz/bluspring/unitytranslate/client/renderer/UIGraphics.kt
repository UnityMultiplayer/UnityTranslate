package xyz.bluspring.unitytranslate.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import java.util.*

class UIGraphics {
    val poseStack = PoseStack()
    private val scissorState = Stack<ScreenRectangle>()
    private val currentScissor: ScreenRectangle?
        get() = if (this.scissorState.isEmpty()) null else this.scissorState.peek()

    val width: Int
        get() = (ClientPlatformProxy.instance.framebuffer.width.toFloat() / ClientPlatformProxy.instance.guiScale.toFloat()).toInt()

    val height: Int
        get() = (ClientPlatformProxy.instance.framebuffer.height.toFloat() / ClientPlatformProxy.instance.guiScale.toFloat()).toInt()

    fun enableScissor(x: Int, y: Int, width: Int, height: Int) {
        this.scissorState.push(ScreenRectangle(x, y, width, height))
    }

    fun disableScissor() {
        this.scissorState.pop()
    }

    fun drawCenteredString(font: Font, text: Component, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text.visualOrderText, x - (font.width(text) / 2f), y, color, dropShadow)

    fun drawCenteredString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text, x - (font.width(text) / 2f), y, color, dropShadow)

    fun drawString(font: Font, text: Component, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text.visualOrderText, x, y, color, dropShadow)

    fun drawString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean) {
        val pose = poseStack.last()
        val prepared = font.prepareText(text, x, y, color, dropShadow, true, 0)
        prepared.visit(object : Font.GlyphVisitor {
            override fun acceptEffect(effect: TextRenderable) {
                accept(effect)
            }

            override fun acceptGlyph(glyph: TextRenderable.Styled) {
                accept(glyph)
            }

            private fun accept(glyph: TextRenderable) {
                val consumer = BatchedGuiRenderer.getBuffer(glyph.guiPipeline(), textures = listOf(
                    BatchedGuiRenderer.Texture("Sampler0", glyph.textureView())
                ), scissor = currentScissor)
                glyph.render(pose.pose(), consumer, LightCoordsUtil.FULL_BRIGHT, false)
            }
        })
    }

    fun fill(x1: Float, y1: Float, x2: Float, y2: Float, colorFrom: Int, colorTo: Int = colorFrom)
        = fill(x1, y1, x2, y2, colorFrom, colorFrom, colorTo, colorTo)

    fun fill(x1: Float, y1: Float, x2: Float, y2: Float, colorTopLeft: Int, colorTopRight: Int, colorBottomLeft: Int, colorBottomRight: Int) {
        val pose = poseStack.last()
        val buffer = BatchedGuiRenderer.getBuffer(RenderPipelines.GUI)
        buffer.addVertex(pose, x1, y1, 0f).setColor(colorTopLeft)
        buffer.addVertex(pose, x1, y2, 0f).setColor(colorBottomLeft)
        buffer.addVertex(pose, x2, y2, 0f).setColor(colorBottomRight)
        buffer.addVertex(pose, x2, y1, 0f).setColor(colorTopRight)
    }

    fun outline(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, thickness: Float = 1f) {
        // top
        this.fill(x1, y1, x2, y1 + thickness, color)
        this.fill(x1, y2 - thickness, x2, y2, color)

        // sides
        this.fill(x1, y1 + thickness, x1 + thickness, y2 - thickness, color)
        this.fill(x2 - thickness, y1 + thickness, x2, y2 - thickness, color)
    }
}
