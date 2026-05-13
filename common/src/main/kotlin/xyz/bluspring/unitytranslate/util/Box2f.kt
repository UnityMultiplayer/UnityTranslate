package xyz.bluspring.unitytranslate.util

import org.joml.Vector2f

data class Box2f(
    val top: Float, val left: Float,
    val bottom: Float, val right: Float
) {
    companion object {
        @JvmStatic
        fun expandFromAnchor(anchor: Vector2f, width: Float, height: Float): Box2f {
            val topInfluence = anchor.y
            val bottomInfluence = 1f - anchor.y
            val leftInfluence = anchor.x
            val rightInfluence = 1f - anchor.x

            return Box2f(
                topInfluence * height, leftInfluence * width,
                bottomInfluence * height, rightInfluence * width
            )
        }
    }
}
