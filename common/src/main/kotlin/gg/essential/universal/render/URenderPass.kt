package gg.essential.universal.render

import com.mojang.blaze3d.GpuFormat
import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.AddressMode
import com.mojang.blaze3d.textures.FilterMode
import gg.essential.universal.vertex.UBuiltBuffer
import gg.essential.universal.vertex.UBuiltBufferInternal
import net.minecraft.client.Minecraft
import org.lwjgl.system.MemoryStack
import java.util.*

// Kept internal for now because I'm not yet sure how Mojang will evolve their RenderPass.
// At the moment we can't keep it open across draw calls because MC prevents uploading any new buffers while the render
// pass is active. Not sure if that's intentional.
// For older versions it would definitely be useful to issue multiple draw calls via one URenderPass object because
// we can cache the global GL state between calls and don't need to re-query and reset everything on every draw.
internal class URenderPass : AutoCloseable {

    override fun close() {
    }

    fun draw(builtBuffer: UBuiltBuffer, pipeline: URenderPipeline, configure: (DrawCallBuilder) -> Unit) {
        val builder = DrawCallBuilderImpl(pipeline, builtBuffer as UBuiltBufferInternal)
        configure(builder)
        builder.submit()
    }

    internal inner class DrawCallBuilderImpl(
        private val pipeline: URenderPipeline,
        private val builtBuffer: UBuiltBufferInternal,
    ) : DrawCallBuilder {
        val mc: RenderPass
        init {
            val dynamicUniforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                org.joml.Vector4f(1f, 1f, 1f, 1f),
                org.joml.Vector3f(),
                org.joml.Matrix4f(),
            )
            val builtBuffer = builtBuffer.mc
            val vertexBuffer = pipeline.format.uploadImmediateVertexBuffer(builtBuffer.vertexBuffer())
            val sortedBuffer = builtBuffer.indexBuffer()
            val (indexBuffer, indexType) = if (sortedBuffer != null) {
                pipeline.format.uploadImmediateIndexBuffer(sortedBuffer) to builtBuffer.drawState().indexType()
            } else {
                val shapeIndexBuffer = RenderSystem.getSequentialBuffer(builtBuffer.drawState().mode())
                shapeIndexBuffer.getBuffer(builtBuffer.drawState().indexCount()) to shapeIndexBuffer.type()
            }
            mc = Minecraft.getInstance().mainRenderTarget.let { fb ->
                RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    { "Immediate draw for $pipeline" },
                    RenderSystem.outputColorTextureOverride ?: fb.colorTextureView!!,
                    OptionalInt.empty(),
                    RenderSystem.outputDepthTextureOverride ?: fb.depthTextureView,
                    OptionalDouble.empty(),
                )
            }
            mc.setVertexBuffer(0, vertexBuffer)
            mc.setIndexBuffer(indexBuffer, indexType)
            RenderSystem.bindDefaultUniforms(mc)
            mc.setUniform("DynamicTransforms", dynamicUniforms);
        }

        private var scissor: ScissorState? = null

        override fun noScissor(): DrawCallBuilder = apply {
            scissor = ScissorState.DISABLED
        }

        override fun scissor(x: Int, y: Int, width: Int, height: Int) = apply {
            scissor = ScissorState(true, x, y, width, height)
        }

        private val tmpBuffers = mutableListOf<GpuBuffer>()

        override fun uniform(name: String, vararg values: Float): DrawCallBuilder = apply {
            mc.setUniform(name, MemoryStack.stackPush().use { stack ->
                val byteBuf = stack.malloc(values.size * 4)
                values.forEach { byteBuf.putFloat(it) }
                byteBuf.flip()
                RenderSystem.getDevice().createBuffer({ "$name UBO" }, GpuBuffer.USAGE_UNIFORM, byteBuf)
            }.also { tmpBuffers.add(it) })
        }

        override fun uniform(name: String, vararg values: Int): DrawCallBuilder = apply {
            mc.setUniform(name, MemoryStack.stackPush().use { stack ->
                val byteBuf = stack.malloc(values.size * 4)
                values.forEach { byteBuf.putInt(it) }
                byteBuf.flip()
                RenderSystem.getDevice().createBuffer({ "$name UBO" }, GpuBuffer.USAGE_UNIFORM, byteBuf)
            }.also { tmpBuffers.add(it) })
        }

        override fun texture(name: String, textureGlId: Int): DrawCallBuilder = apply {
            val texture = object : GlTexture(USAGE_TEXTURE_BINDING, "", GpuFormat.RGBA8_UNORM, 0, 0, 0, 1, textureGlId) {
            }
            val sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
            mc.bindTexture(name, RenderSystem.getDevice().createTextureView(texture), sampler)
        }

        override fun texture(index: Int, textureGlId: Int): DrawCallBuilder = apply {
            texture(pipeline.mcRenderPipeline.samplers[index], textureGlId)
        }

        fun submit() {
            val scissor = scissor ?: ScissorState.active()
            if (scissor.enabled) {
                mc.enableScissor(scissor.x, scissor.y, scissor.width, scissor.height)
            } else {
                mc.disableScissor()
            }

            pipeline.draw(mc, builtBuffer.mc)

            mc.close()
            tmpBuffers.forEach { it.close() }
        }
    }
}
