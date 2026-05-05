package gg.essential.universal.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp
import com.mojang.blaze3d.shaders.ShaderType
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.blaze3d.vertex.VertexFormatElement
import gg.essential.universal.UGraphics
import gg.essential.universal.UGraphics.CommonVertexFormats
import gg.essential.universal.UGraphics.DrawMode
import gg.essential.universal.shader.BlendState
import gg.essential.universal.shader.ShaderTransformer
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import org.apache.commons.codec.digest.DigestUtils


typealias ShaderSourceGetter = com.mojang.blaze3d.shaders.ShaderSource

class URenderPipeline private constructor(
    private val id: Identifier,
    internal val format: VertexFormat,
    private var shaderSourceGetter: ShaderSourceGetter?,
    internal val mcRenderPipeline: RenderPipeline,
) {
    internal fun draw(renderPass: RenderPass, builtBuffer: MeshData) {
        if (shaderSourceGetter != null) {
            // Supply our shader sources to the render backend, need to do this each draw (it'll no-op if it's already
            // cached) because resource reloads will clear it again.
            RenderSystem.getDevice().precompilePipeline(mcRenderPipeline, shaderSourceGetter)
        }
        renderPass.setPipeline(mcRenderPipeline)

        renderPass.drawIndexed(0, 0, builtBuffer.drawState().indexCount, 1)
    }

    override fun toString(): String {
        return id.toString()
    }

    enum class DepthTest {
        Disabled,
        Always,
        Equal,
        LessOrEqual,
        Less,
        Greater,
        GreaterOrEqual,
        NotEqual,
        Never,
        ;

    }

    enum class ColorLogic {
        None,
        OrReverse,
    }

    private sealed interface ShaderSupplier {

        class LegacySource(val vertexFormat: VertexFormat, val vertSource: String, val fragSource: String) : ShaderSupplier {
        }

        class Mc(val vert: Identifier, val frag: Identifier, val samplers: List<String>, val uniforms: Map<String, UniformType>) : ShaderSupplier
    }

    interface BuilderProps {
        var depthTest: DepthTest
        var culling: Boolean
        @Deprecated("Unsupported as of Minecraft 26.1")
        var colorLogic: ColorLogic
        var blendState: BlendState
        var colorMask: Pair</*rgb*/Boolean, /*alpha*/Boolean>
        var depthMask: Boolean
        var polygonOffset: Pair</*factor*/Float, /*units*/Float>
    }

    interface Builder : BuilderProps {
        fun build(): URenderPipeline
    }

    private data class BuilderPropsImpl(
        override var depthTest: DepthTest,
        override var culling: Boolean,
        @Deprecated("Unsupported as of Minecraft 26.1")
        override var colorLogic: ColorLogic,
        override var blendState: BlendState,
        override var colorMask: Pair<Boolean, Boolean>,
        override var depthMask: Boolean,
        override var polygonOffset: Pair<Float, Float>,
    ) : BuilderProps {
        constructor() : this(
            depthTest = DepthTest.Disabled,
            culling = false,
            colorLogic = ColorLogic.None,
            blendState = BlendState.DISABLED,
            colorMask = enabledColorMask,
            depthMask = true,
            polygonOffset = zeroPolygonOffset,
        )

        private companion object {
            private val enabledColorMask = Pair(true, true)
            private val zeroPolygonOffset = Pair(0f, 0f)
        }
    }

    private class BuilderImpl(
        private val id: Identifier,
        private val drawMode: DrawMode,
        private val format: VertexFormat,
        private val shader: ShaderSupplier,
    ) : Builder, BuilderProps by BuilderPropsImpl() {
        override fun build(): URenderPipeline {
            var shaderSourceGetter: ShaderSourceGetter? = null
            var mcRenderPipeline = RenderPipeline.builder().apply {
                withLocation(id)
                withVertexFormat(format, drawMode.mcMode)
                when (shader) {
                    is ShaderSupplier.LegacySource -> {
                        val transformer = ShaderTransformer(format, 150)

                        val transformedVertSource = transformer.transform(shader.vertSource)
                        val transformedFragSource = transformer.transform(shader.fragSource)

                        val vertId = Identifier.fromNamespaceAndPath("universalcraft", "shader/generated/" + DigestUtils.sha1Hex(transformedVertSource).lowercase())
                        val fragId = Identifier.fromNamespaceAndPath("universalcraft", "shader/generated/" + DigestUtils.sha1Hex(transformedFragSource).lowercase())

                        shaderSourceGetter = ShaderSourceGetter { id: Identifier, type: ShaderType ->
                            when (id) {
                                vertId -> transformedVertSource
                                fragId -> transformedFragSource
                                else -> Minecraft.getInstance().shaderManager.getShader(id, type)
                            }
                        }

                        withVertexShader(vertId)
                        withFragmentShader(fragId)

                        transformer.samplers.forEach { withSampler(it) }
                        transformer.uniforms.forEach { withUniform(it.key, it.value.mc) }

                        // ShaderProgram calls glBindAttribLocation using the names in the VertexFormat so we need to
                        // construct a custom one based on the original but with our prefixed names
                        val builder = VertexFormat.builder()
                        var expectedOffset = 0
                        format.elements.mapIndexed { index, element ->
                            val offset = format.getOffset(element)
                            val padding = offset - expectedOffset
                            if (padding > 0) {
                                expectedOffset += padding
                                builder.padding(padding)
                            }
                            expectedOffset += element.byteSize()
                            val name = transformer.attributes.getOrNull(index) ?: format.getElementName(element)
                            builder.add(name, element)
                        }
                        withVertexFormat(builder.build(), drawMode.mcMode)
                    }
                    is ShaderSupplier.Mc -> {
                        withVertexShader(shader.vert)
                        withFragmentShader(shader.frag)

                        shader.samplers.forEach { withSampler(it) }
                        shader.uniforms.forEach { withUniform(it.key, it.value) }
                    }
                }
                if (depthTest != DepthTest.Disabled) {
                    withDepthStencilState(DepthStencilState(when (depthTest) {
                        DepthTest.Disabled -> throw AssertionError("unreachable")
                        DepthTest.Always -> CompareOp.ALWAYS_PASS
                        DepthTest.Equal -> CompareOp.EQUAL
                        DepthTest.LessOrEqual -> CompareOp.LESS_THAN_OR_EQUAL
                        DepthTest.Less -> CompareOp.LESS_THAN
                        DepthTest.Greater -> CompareOp.GREATER_THAN
                        DepthTest.GreaterOrEqual -> CompareOp.GREATER_THAN_OR_EQUAL
                        DepthTest.NotEqual -> CompareOp.NOT_EQUAL
                        DepthTest.Never -> CompareOp.NEVER_PASS
                    }, depthMask, polygonOffset.first, polygonOffset.second))
                }
                withCull(culling)
                withColorTargetState(ColorTargetState(
                    java.util.Optional.ofNullable(if (blendState.enabled) BlendFunction(
                        blendState.srcRgb.mcSourceFactor,
                        blendState.dstRgb.mcDestFactor,
                        blendState.srcAlpha.mcSourceFactor,
                        blendState.dstAlpha.mcDestFactor,
                    ) else null),
                    colorMask.let { (colorMask, alphaMask) ->
                        var flags = 0
                        if (colorMask) flags += ColorTargetState.WRITE_COLOR
                        if (alphaMask) flags += ColorTargetState.WRITE_ALPHA
                        flags
                    }
                ))
            }.build()


            return URenderPipeline(
                id,
                format,
                shaderSourceGetter,
                mcRenderPipeline,
            )
        }
    }

    companion object {
        val isRequired = true


        @JvmStatic
        fun wrap(mc: RenderPipeline): URenderPipeline =
            URenderPipeline(mc.location, mc.vertexFormat, null, mc)
        fun builder(id: Identifier, drawMode: DrawMode, format: VertexFormat, vert: Identifier, frag: Identifier, samplers: List<String>, uniforms: Map<String, UniformType>): Builder {
            return BuilderImpl(id, drawMode, format, ShaderSupplier.Mc(vert, frag, samplers, uniforms))
        }

        fun builderWithDefaultShader(id: String, drawMode: DrawMode, format: CommonVertexFormats): Builder {
            val mcId = Identifier.parse(id)
            val shader = UGraphics.DEFAULT_SHADERS[format.mc]
                ?: throw IllegalArgumentException("No default shader for $format.")
            val shaderId = Identifier.withDefaultNamespace(shader)
            val samplers = List(format.mc.elements.count {
                it == VertexFormatElement.UV0 || it == VertexFormatElement.UV1 || it == VertexFormatElement.UV2
            }) { i -> "Sampler$i" }
            val uniforms = mapOf(
                "DynamicTransforms" to UniformType.UNIFORM_BUFFER,
                "Projection" to UniformType.UNIFORM_BUFFER,
            )
            return builder(mcId, drawMode, format.mc, shaderId, shaderId, samplers, uniforms)
        }

        fun builderWithLegacyShader(id: String, drawMode: DrawMode, format: CommonVertexFormats, vertSource: String, fragSource: String): Builder {
            return builderWithLegacyShader(id, drawMode, format.mc, vertSource, fragSource)
        }

        fun builderWithLegacyShader(id: String, drawMode: DrawMode, format: VertexFormat, vertSource: String, fragSource: String): Builder {
            val mcId = Identifier.parse(id)
            return BuilderImpl(mcId, drawMode, format, ShaderSupplier.LegacySource(format, vertSource, fragSource))
        }
    }
}
