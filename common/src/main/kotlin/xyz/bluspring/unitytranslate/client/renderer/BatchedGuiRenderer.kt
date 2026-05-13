package xyz.bluspring.unitytranslate.client.renderer

import com.mojang.blaze3d.ProjectionType
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.Projection
import net.minecraft.client.renderer.ProjectionMatrixBuffer
import net.minecraft.client.renderer.StagedVertexBuffer
import net.minecraft.client.renderer.rendertype.PreparedRenderType
import net.minecraft.util.profiling.Profiler
import org.joml.Matrix4f
import org.joml.getVector3f
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import java.util.*

object BatchedGuiRenderer {
    private val vertexBuffer = StagedVertexBuffer({ "UnityTranslate GUI Vertex Buffer" }, 786432)
    private val draws = mutableListOf<QueuedDraw>()
    private val projection = Projection()
    private val projectionBuffer = ProjectionMatrixBuffer("unitytranslate_gui")

    private var lastDraw: StagedVertexBuffer.Draw? = null
    private var lastPipeline: RenderPipeline? = null
    private var lastScissorArea: ScreenRectangle? = null
    private var lastTextureSetup: TextureSetup? = null

    private fun scissorChanged(newScissor: ScreenRectangle?, oldScissor: ScreenRectangle?): Boolean {
        if (newScissor == oldScissor)
            return false

        if (newScissor != null)
            return newScissor != oldScissor

        return true
    }

    fun queue(prepared: PreparedRenderType, buffer: MeshData) {
        this.queue(prepared.pipeline, TextureSetup(
            prepared.textures.getOrNull(0)?.textureView,
            prepared.textures.getOrNull(1)?.textureView,
            prepared.textures.getOrNull(2)?.textureView,
            prepared.textures.getOrNull(0)?.sampler,
            prepared.textures.getOrNull(1)?.sampler,
            prepared.textures.getOrNull(2)?.sampler,
        ), if (prepared.scissorState.enabled())
            ScreenRectangle(prepared.scissorState.x(), prepared.scissorState.y(), prepared.scissorState.width(), prepared.scissorState.height())
        else null) { consumer ->
           copyBuffer(buffer, consumer)
        }
    }

    @JvmStatic
    fun copyBuffer(builtBuffer: MeshData, consumer: VertexConsumer) {
        val vertexBuffer = builtBuffer.vertexBuffer()

        for (vtxId in 0 until builtBuffer.drawState().vertexCount) {
            val stride = vtxId * builtBuffer.drawState().format.vertexSize

            for (element in builtBuffer.drawState().format.elements) {
                when (element.name) {
                    "Position" -> consumer.addVertex(vertexBuffer.getVector3f(stride + element.offset))
                    "Color" -> consumer.setColor(vertexBuffer.getInt(stride + element.offset))
                    "UV0" -> consumer.setUv(vertexBuffer.getFloat(stride + element.offset), vertexBuffer.getFloat(stride + element.offset + 4))
                    "UV1" -> consumer.setUv1(vertexBuffer.getInt(stride + element.offset), vertexBuffer.getInt(stride + element.offset + 4))
                    "UV2" -> consumer.setUv2(vertexBuffer.getInt(stride + element.offset), vertexBuffer.getInt(stride + element.offset + 4))
                    "Normal" -> consumer.setNormal(vertexBuffer.getFloat(stride + element.offset), vertexBuffer.getFloat(stride + element.offset + 4), vertexBuffer.getFloat(stride + element.offset + 8))
                    "LineWidth" -> consumer.setLineWidth(vertexBuffer.getFloat(stride + element.offset))
                }
            }
        }
    }

    private fun prepareQueue(pipeline: RenderPipeline, textureSetup: TextureSetup, scissorArea: ScreenRectangle?) {
        if (this.lastDraw == null || this.lastPipeline != pipeline || this.scissorChanged(scissorArea, this.lastScissorArea) || textureSetup != this.lastTextureSetup) {
            this.lastPipeline = pipeline
            this.lastTextureSetup = textureSetup
            this.lastScissorArea = scissorArea
            this.lastDraw = this.vertexBuffer.appendDraw(pipeline.getVertexFormatBinding(0)!!, pipeline.primitiveTopology)
            this.draws.add(QueuedDraw(this.lastDraw!!, pipeline, textureSetup, scissorArea))
        }
    }

    fun queue(pipeline: RenderPipeline, textureSetup: TextureSetup, scissorArea: ScreenRectangle?, vertexBuilder: (VertexConsumer) -> Unit) {
        prepareQueue(pipeline, textureSetup, scissorArea)
        vertexBuilder.invoke(this.vertexBuffer.getVertexBuilder(this.lastDraw!!))
    }

    fun render() {
        val profiler = Profiler.get()

        profiler.push("prepare")
        val pipeline = this.lastPipeline
        val textureSetup = this.lastTextureSetup
        val scissorArea = this.lastScissorArea

        if (pipeline != null && textureSetup != null) {
            this.lastDraw = this.vertexBuffer.appendDraw(pipeline.getVertexFormatBinding(0)!!, pipeline.primitiveTopology)
            this.draws.add(QueuedDraw(this.lastDraw!!, pipeline, textureSetup, scissorArea))
        }

        if (ClientPlatformProxy.instance.renderGui()) {
            profiler.popPush("upload")
            this.vertexBuffer.upload()
        }

        profiler.popPush("draw")
        this.draw()

        profiler.popPush("endFrame")
        this.vertexBuffer.endDraw()
        this.vertexBuffer.endFrame()
        this.draws.clear()

        // reset
        this.lastDraw = null
        this.lastPipeline = null
        this.lastScissorArea = null
        this.lastTextureSetup = null

        profiler.pop()
    }

    private fun draw() {
        if (this.draws.isNotEmpty()) {
            val framebuffer = ClientPlatformProxy.instance.framebuffer
            this.projection.setupOrtho(1000f, 11000f, framebuffer.width.toFloat(), framebuffer.height.toFloat(), true)
            RenderSystem.setProjectionMatrix(this.projectionBuffer.getBuffer(this.projection), ProjectionType.ORTHOGRAPHIC)

            val dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(Matrix4f().setTranslation(0f, 0f, -11000f))

            RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass({ "UnityTranslate GUI Renderer" }, framebuffer.colorTextureView!!, Optional.empty(), if (framebuffer.useDepth) framebuffer.depthTextureView!! else null,
                    OptionalDouble.empty())
                .use { renderPass ->
                    RenderSystem.bindDefaultUniforms(renderPass)
                    renderPass.setUniform("DynamicTransforms", dynamicTransforms)

                    for (draw in this.draws) {
                        val executeInfo = this.vertexBuffer.getExecuteInfo(draw.draw) ?: continue
                        renderPass.setPipeline(draw.pipeline)
                        renderPass.setVertexBuffer(0, executeInfo.vertexBuffer.slice())
                        val scissorArea = draw.scissorArea
                        if (scissorArea != null) {
                            renderPass.enableScissor(scissorArea.left(), framebuffer.height - scissorArea.bottom(), 0.coerceAtLeast((scissorArea.right() - scissorArea.left())), 0.coerceAtLeast(scissorArea.bottom() - scissorArea.top()))
                        } else {
                            renderPass.disableScissor()
                        }

                        if (draw.textureSetup.texure0 != null) {
                            renderPass.bindTexture("Sampler0", draw.textureSetup.texure0, draw.textureSetup.sampler0)
                        }

                        if (draw.textureSetup.texure1 != null) {
                            renderPass.bindTexture("Sampler0", draw.textureSetup.texure1, draw.textureSetup.sampler1)
                        }

                        if (draw.textureSetup.texure2 != null) {
                            renderPass.bindTexture("Sampler2", draw.textureSetup.texure2, draw.textureSetup.sampler2)
                        }

                        renderPass.setIndexBuffer(executeInfo.indexBuffer, executeInfo.indexType)
                        renderPass.drawIndexed(executeInfo.baseVertex, executeInfo.firstIndex, executeInfo.indexCount, 1)
                    }
                }
        }
    }

    @JvmRecord
    private data class QueuedDraw(val draw: StagedVertexBuffer.Draw, val pipeline: RenderPipeline, val textureSetup: TextureSetup, val scissorArea: ScreenRectangle?)
}
