package gg.essential.universal.vertex

import com.mojang.blaze3d.vertex.VertexConsumer
import gg.essential.universal.UMatrixStack
import org.joml.Vector3f

internal open class VanillaVertexConsumer(
    private val inner: VertexConsumer,
) : UVertexConsumer {

    override fun pos(stack: UMatrixStack, x: Double, y: Double, z: Double): UVertexConsumer = apply {
        if (stack === UMatrixStack.UNIT) {
            inner.addVertex(x.toFloat(), y.toFloat(), z.toFloat())
            return@apply
        }
        inner.addVertex(stack.peek().model, x.toFloat(), y.toFloat(), z.toFloat())
    }

    override fun color(red: Int, green: Int, blue: Int, alpha: Int): UVertexConsumer = apply {
        inner.setColor(red, green, blue, alpha)
    }

    override fun tex(u: Double, v: Double): UVertexConsumer = apply {
        inner.setUv(u.toFloat(), v.toFloat());
    }

    override fun overlay(u: Int, v: Int): UVertexConsumer = apply {
        inner.setUv1(u, v);
    }

    override fun light(u: Int, v: Int): UVertexConsumer = apply {
        inner.setUv2(u, v)
    }

    override fun norm(stack: UMatrixStack, x: Float, y: Float, z: Float): UVertexConsumer = apply {
        if (stack === UMatrixStack.UNIT) {
            inner.setNormal(x, y, z)
            return@apply
        }
        val normal = stack.peek().normal.transform(x, y, z, Vector3f())
        inner.setNormal(normal.x, normal.y, normal.z)
    }

    override fun endVertex(): UVertexConsumer = apply {
    }
}
