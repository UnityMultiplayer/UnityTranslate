package xyz.bluspring.unitytranslate.client.renderer.ui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.renderer.BatchedGuiRenderer
import xyz.bluspring.unitytranslate.client.renderer.ui.texture.TextureReference
import java.util.*

class BatchedUIGraphics(private val layer: BatchedGuiRenderer.DrawLayer) : UIGraphics {
    val poseStack = PoseStack()
    private val scissorState = Stack<ScreenRectangle>()
    private val currentScissor: ScreenRectangle?
        get() = if (this.scissorState.isEmpty()) null else this.scissorState.peek()

    override val width: Int
        get() = (ClientPlatformProxy.instance.framebuffer.width.toFloat() / ClientPlatformProxy.instance.guiScale.toFloat()).toInt()

    override val height: Int
        get() = (ClientPlatformProxy.instance.framebuffer.height.toFloat() / ClientPlatformProxy.instance.guiScale.toFloat()).toInt()

    override fun enableScissor(x: Int, y: Int, width: Int, height: Int) {
        this.scissorState.push(ScreenRectangle(x, y, width, height))
    }

    override fun disableScissor() {
        this.scissorState.pop()
    }

    override fun drawString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean) {
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
                ), scissor = currentScissor, layer = this@BatchedUIGraphics.layer)
                glyph.render(pose.pose(), consumer, LightCoordsUtil.FULL_BRIGHT, false)
            }
        })
    }

    override fun fill(x1: Float, y1: Float, x2: Float, y2: Float, colorTopLeft: Int, colorTopRight: Int, colorBottomLeft: Int, colorBottomRight: Int) {
        val pose = poseStack.last()
        val buffer = BatchedGuiRenderer.getBuffer(RenderPipelines.GUI, layer = this.layer)
        buffer.addVertex(pose, x1, y1, 0f).setColor(colorTopLeft)
        buffer.addVertex(pose, x1, y2, 0f).setColor(colorBottomLeft)
        buffer.addVertex(pose, x2, y2, 0f).setColor(colorBottomRight)
        buffer.addVertex(pose, x2, y1, 0f).setColor(colorTopRight)
    }

    override fun blitWithColor(
        x: Float, y: Float, width: Float, height: Float,
        u: Float, v: Float, uWidth: Float, vHeight: Float,
        texture: TextureReference,
        colorTopLeft: Int, colorTopRight: Int,
        colorBottomLeft: Int, colorBottomRight: Int
    ) {
        val pose = poseStack.last()
        val buffer = BatchedGuiRenderer.getBuffer(RenderPipelines.GUI_TEXTURED, listOf(BatchedGuiRenderer.Texture("Sampler0", texture.textureView)), scissor = currentScissor, layer = this@BatchedUIGraphics.layer)

        val u0 = ((u * texture.width) + (texture.u0 * texture.imageWidth)) / texture.imageWidth
        val v0 = ((v * texture.height) + (texture.v0 * texture.imageHeight)) / texture.imageHeight
        val u1 = (((u + uWidth) * texture.width) + (texture.u1 * texture.imageWidth)) / texture.imageWidth
        val v1 = (((v + vHeight) * texture.height) + (texture.v1 * texture.imageHeight)) / texture.imageHeight

        buffer.addVertex(pose, x, y, 0f)
            .setUv(u0, v0)
            .setColor(colorTopLeft)
        buffer.addVertex(pose, x, y + height, 0f)
            .setUv(u0, v1)
            .setColor(colorBottomLeft)
        buffer.addVertex(pose, x + width, y + height, 0f)
            .setUv(u1, v1)
            .setColor(colorBottomRight)
        buffer.addVertex(pose, x + width, y, 0f)
            .setUv(u1, v0)
            .setColor(colorTopRight)
    }

    override fun pushMatrix() = this.poseStack.pushPose()
    override fun translate(x: Float, y: Float) = this.poseStack.translate(x, y, 0f)
    override fun scale(x: Float, y: Float) = this.poseStack.scale(x, y, 1f)
    override fun popMatrix() = this.poseStack.popPose()
}

