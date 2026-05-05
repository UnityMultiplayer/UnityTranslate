package gg.essential.universal

import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.universal.utils.TemporaryTextureAllocator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.Identifier
import org.joml.Matrix4f

/**
 * Allows rendering of raw OpenGL into [DrawContext] by drawing to a temporary texture which is then submitted as a
 * plain textured quad to [DrawContext].
 *
 * You **MUST** call [nextFrame] before the next frame begins but no sooner than MC's GuiRenderer actually using the
 * submitted textures. Repeated calls to [drawImmediate] without [nextFrame] or [close] inbetween will keep allocating
 * more and more gpu memory!
 * If you cannot guarantee further calls to [nextFrame], you must call [close] to release all resources.
 * The [AdvancedDrawContext] remains usable, [close] merely frees all resources, further calls to [drawImmediate] will
 * simply re-allocate them.
 */
internal class AdvancedDrawContext : AutoCloseable {
    private val projection = Matrix4f()
    private var allocatedProjectionMatrix: ProjectionMatrixBuffer? = null

    private val textureAllocator = TemporaryTextureAllocator {
        allocatedProjectionMatrix?.close()
        allocatedProjectionMatrix = null
    }

    fun drawImmediate(context: GuiGraphicsExtractor, block: (UMatrixStack) -> Unit) {
        val scaleFactor = UResolution.scaleFactor.toFloat()
        val width = UResolution.viewportWidth
        val height = UResolution.viewportHeight

        val texture = textureAllocator.allocate(width, height)

        var projectionMatrix = allocatedProjectionMatrix
        if (projectionMatrix == null) {
            projectionMatrix = ProjectionMatrixBuffer("pre-rendered screen")
            allocatedProjectionMatrix = projectionMatrix
        }
        projection.setOrtho(
            0f, width.toFloat() / scaleFactor,
            height.toFloat() / scaleFactor, 0f,
            1000f, 21000f,
            RenderSystem.getDevice().deviceInfo.isZZeroToOne,
        )
        val projectionMatrixBuffer = projectionMatrix.getBuffer(projection)
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer, ProjectionType.ORTHOGRAPHIC)

        val orgOutputColorTextureOverride = RenderSystem.outputColorTextureOverride
        val orgOutputDepthTextureOverride = RenderSystem.outputDepthTextureOverride
        RenderSystem.outputColorTextureOverride = texture.textureView
        RenderSystem.outputDepthTextureOverride = texture.depthTextureView

        val matrixStack = UMatrixStack()
        matrixStack.translate(0f, 0f, -10000f)
        block(matrixStack)

        RenderSystem.outputColorTextureOverride = orgOutputColorTextureOverride
        RenderSystem.outputDepthTextureOverride = orgOutputDepthTextureOverride

        draw(context, texture)
    }

    fun draw(context: GuiGraphicsExtractor, texture: TemporaryTextureAllocator.TextureAllocation) {
        val width = texture.width
        val height = texture.height
        val scaleFactor = UResolution.scaleFactor.toFloat()

        val textureManager = Minecraft.getInstance().textureManager
        val identifier = Identifier.fromNamespaceAndPath("universalcraft", "__tmp_texture__")
        textureManager.register(identifier, object : AbstractTexture() {
            init { textureView = texture.textureView }
            override fun close() {} // we don't want the later `destroyTexture` to close our texture
        })

        context.pose().pushMatrix()
        context.pose().scale(1/scaleFactor, 1/scaleFactor) // drawTexture only accepts `int`s
        context.blit(
            RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
            identifier,
            // x, y
            0, 0,
            // u, v
            0f, height.toFloat(),
            // width, height
            width, height,
            // uWidth, vHeight
            width, -height,
            // textureWidth, textureHeight
            width, height,
        )
        context.pose().popMatrix()

        textureManager.release(identifier)
    }

    fun nextFrame() {
        textureAllocator.nextFrame()
    }

    override fun close() {
        textureAllocator.close()
    }
}
