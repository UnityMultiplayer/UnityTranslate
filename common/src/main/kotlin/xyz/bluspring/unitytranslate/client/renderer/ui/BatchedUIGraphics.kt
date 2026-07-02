package xyz.bluspring.unitytranslate.client.renderer.ui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import net.minecraft.util.Mth
import org.joml.Quaternionf
import xyz.bluspring.unitytranslate.api.v2.client.gui.TextureReference
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.renderer.BatchedGuiRenderer
import xyz.bluspring.unitytranslate.client.renderer.ui.font.MinecraftFontReference
import xyz.bluspring.unitytranslate.client.renderer.ui.texture.AbstractTextureReference
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

    override fun text(font: FontReference, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean) {
        val pose = poseStack.last()
        val prepared = (font as MinecraftFontReference).font.prepareText(text, x, y, color, dropShadow, true, 0)
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
        x1: Float, y1: Float, x2: Float, y2: Float,
        u0: Float, v0: Float, u1: Float, v1: Float,
        texture: TextureReference,
        colorTopLeft: Int, colorTopRight: Int,
        colorBottomLeft: Int, colorBottomRight: Int
    ) {
        if (texture !is AbstractTextureReference)
            throw IllegalStateException("You are not supposed to extend TextureReference! Currently using ${texture::class.java.name}")

        val pose = poseStack.last()
        val buffer = BatchedGuiRenderer.getBuffer(RenderPipelines.GUI_TEXTURED, listOf(BatchedGuiRenderer.Texture("Sampler0", texture.textureView)), scissor = currentScissor, layer = this@BatchedUIGraphics.layer)

        val uStart = ((u0 * texture.width) + (texture.u0 * texture.imageWidth)) / texture.imageWidth
        val vStart = ((v0 * texture.height) + (texture.v0 * texture.imageHeight)) / texture.imageHeight
        val uEnd = ((u1 * texture.width) + (texture.u0 * texture.imageWidth)) / texture.imageWidth
        val vEnd = ((v1 * texture.height) + (texture.v0 * texture.imageHeight)) / texture.imageHeight

        buffer.addVertex(pose, x1, y1, 0f)
            .setUv(uStart, vStart)
            .setColor(colorTopLeft)
        buffer.addVertex(pose, x1, y2, 0f)
            .setUv(uStart, vEnd)
            .setColor(colorBottomLeft)
        buffer.addVertex(pose, x2, y2, 0f)
            .setUv(uEnd, vEnd)
            .setColor(colorBottomRight)
        buffer.addVertex(pose,  x2, y1, 0f)
            .setUv(uEnd, vStart)
            .setColor(colorTopRight)
    }

    override fun meshFill(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float,
        color1: Int, color2: Int,
        color3: Int, color4: Int
    ) {
        val pose = this.poseStack.last().pose()
        val consumer = BatchedGuiRenderer.getBuffer(RenderPipelines.GUI, scissor = currentScissor, layer = this@BatchedUIGraphics.layer)
        consumer.addVertex(pose, x1, y1, 0f) // top left
            .setColor(color1)
        consumer.addVertex(pose, x3, y3, 0f) // bottom left
            .setColor(color3)
        consumer.addVertex(pose, x4, y4, 0f) // bottom right
            .setColor(color4)
        consumer.addVertex(pose, x2, y2, 0f) // top right
            .setColor(color2)
    }

    override fun meshBlitWithColor(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float,

        u1: Float, v1: Float, u2: Float, v2: Float,
        u3: Float, v3: Float, u4: Float, v4: Float,
        texture: TextureReference,
        color1: Int, color2: Int,
        color3: Int, color4: Int
    ) {
        if (texture !is AbstractTextureReference)
            throw IllegalStateException("You are not supposed to extend TextureReference! Currently using ${texture::class.java.name}")

        val pose = this.poseStack.last().pose()
        val consumer = BatchedGuiRenderer.getBuffer(RenderPipelines.GUI_TEXTURED, listOf(BatchedGuiRenderer.Texture("Sampler0", texture.textureView)), scissor = currentScissor, layer = this@BatchedUIGraphics.layer)
        consumer.addVertex(pose, x1, y1, 0f) // top left
            .setUv(u1, v1)
            .setColor(color1)
        consumer.addVertex(pose, x3, y3, 0f) // bottom left
            .setUv(u3, v3)
            .setColor(color3)
        consumer.addVertex(pose, x4, y4, 0f) // bottom right
            .setUv(u4, v4)
            .setColor(color4)
        consumer.addVertex(pose, x2, y2, 0f) // top right
            .setUv(u2, v2)
            .setColor(color2)
    }

    override fun pushMatrix() = this.poseStack.pushPose()
    override fun translate(x: Float, y: Float) = this.poseStack.translate(x, y, 0f)
    override fun rotate(degrees: Float) = this.poseStack.mulPose(Quaternionf().rotateXYZ(degrees * Mth.DEG_TO_RAD, 0f, 0f))
    override fun scale(x: Float, y: Float) = this.poseStack.scale(x, y, 1f)
    override fun popMatrix() = this.poseStack.popPose()
}

