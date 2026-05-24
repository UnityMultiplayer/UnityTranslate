package xyz.bluspring.unitytranslate.api.v2.util

import kotlin.math.floor

/**
 * ARGB colour math helpers.
 */
object ARGBHelper {
    const val MAX_COMPONENT_SIZE = 0xFF

    @JvmStatic
    fun Int.alpha(): Int {
        return (this shr 24) and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun Int.red(): Int {
        return (this shr 16) and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun Int.green(): Int {
        return (this shr 8) and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun Int.blue(): Int {
        return this and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun color(a: Int, r: Int, g: Int, b: Int): Int {
        // 0xFF_FF_FF_FF
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    @JvmStatic
    fun colorFromFloat(a: Float, r: Float, g: Float, b: Float): Int {
        return color((a * MAX_COMPONENT_SIZE).toInt(), (r * MAX_COMPONENT_SIZE).toInt(), (g * MAX_COMPONENT_SIZE).toInt(), (b * MAX_COMPONENT_SIZE).toInt())
    }

    @JvmStatic
    fun srgbLerp(argb1: Int, argb2: Int, delta: Float): Int {
        val a = lerp(argb1.alpha(), argb2.alpha(), delta)
        val r = lerp(argb1.red(), argb2.red(), delta)
        val g = lerp(argb1.green(), argb2.green(), delta)
        val b = lerp(argb1.blue(), argb2.blue(), delta)
        return color(a, r, g, b)
    }

    private fun lerp(from: Int, to: Int, delta: Float): Int {
        return from + floor(delta * (to - from).toFloat()).toInt()
    }
}
