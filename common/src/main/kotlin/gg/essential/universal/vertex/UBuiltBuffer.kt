package gg.essential.universal.vertex

import com.mojang.blaze3d.vertex.MeshData
import gg.essential.universal.render.DrawCallBuilder
import gg.essential.universal.render.URenderPass
import gg.essential.universal.render.URenderPipeline
import net.minecraft.client.renderer.rendertype.RenderType
import org.jetbrains.annotations.ApiStatus.NonExtendable
import xyz.bluspring.unitytranslate.client.renderer.BatchedGuiRenderer

/**
 * A list of vertices to be rendered.
 *
 * Note that instances of this class should generally be used immediately and may not be valid across frames.
 * Note that drawing the same buffer multiple times is not currently supported (but may be in the future).
 */
@NonExtendable // will be cast to UBuiltBufferInternal
interface UBuiltBuffer : AutoCloseable {
    fun drawAndClose(pipeline: URenderPipeline, configure: DrawCallBuilder.() -> Unit = {}): Unit =
        use { draw(pipeline, configure) }

    fun draw(pipeline: URenderPipeline, configure: DrawCallBuilder.() -> Unit = {}) {
        URenderPass().use { renderPass ->
            renderPass.draw(this, pipeline, configure)
        }
    }

    fun drawAndClose(renderLayer: RenderType): Unit =
        use { draw(renderLayer) }

    fun draw(renderLayer: RenderType) {
        val mc = (this as UBuiltBufferInternal).mc
        BatchedGuiRenderer.queue(renderLayer.prepare(), mc)
    }

    companion object {
        /** Wraps the given MC buffer into a [UBuiltBuffer]. */
        @JvmStatic
        fun wrap(mc: MeshData): UBuiltBuffer = WrapperImpl(mc)

        private class WrapperImpl(override val mc: MeshData) : UBuiltBufferInternal {
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
