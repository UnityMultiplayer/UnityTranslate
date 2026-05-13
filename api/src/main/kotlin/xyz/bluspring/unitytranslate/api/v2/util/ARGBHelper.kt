package xyz.bluspring.unitytranslate.api.v2.util

object ARGBHelper {
    const val MAX_COMPONENT_SIZE = 0xFF

    @JvmStatic
    fun alpha(color: Int): Int {
        return (color shr 24) and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun red(color: Int): Int {
        return (color shr 16) and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun green(color: Int): Int {
        return (color shr 8) and MAX_COMPONENT_SIZE
    }

    @JvmStatic
    fun blue(color: Int): Int {
        return color and MAX_COMPONENT_SIZE
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
}
