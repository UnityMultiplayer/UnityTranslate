package gg.essential.universal.vertex


import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.VertexFormat
import gg.essential.universal.UGraphics

/**
 * Builds a list of vertices to be rendered.
 *
 * Note that once a builder has been created, it must be completed with a call to [build], it may not simply be dropped.
 * Note that only a single builder may be active at any one time (this restriction may be lifted in the future).
 * Note that instances of this class should generally be used immediately and may not be valid across frames.
 */
interface UBufferBuilder : UVertexConsumer {
    /**
     * Finishes the builder. Returns `null` if no vertices were emitted.
     *
     * Calling any other method on this builder after this one, or calling this one multiple times, is undefined
     * behavior.
     */
    fun build(): UBuiltBuffer?

    companion object {
        private val sharedBuffer = ByteBufferBuilder(64 * 1024)

        @JvmStatic
        fun create(drawMode: UGraphics.DrawMode, format: UGraphics.CommonVertexFormats): UBufferBuilder =
            create(drawMode, format.mc)

        @JvmStatic
        fun create(drawMode: UGraphics.DrawMode, format: VertexFormat): UBufferBuilder {
            val mcBufferBuilder = BufferBuilder(sharedBuffer, drawMode.mcMode, format)
            return UBufferBuilderImpl(mcBufferBuilder)
        }

        private class UBufferBuilderImpl(val mc: BufferBuilder) : VanillaVertexConsumer(mc), UBufferBuilder {
            override fun build(): UBuiltBuffer? =
                mc.build()?.let { UBuiltBufferImpl(it) }
        }

        private class UBuiltBufferImpl(override val mc: MeshData) : UBuiltBufferInternal {
            private var closed = false
            override fun close() {
                if (closed) return
                closed = true

                mc.close()
            }

            override fun closedExternally() {
                closed = true
            }
        }
    }
}
