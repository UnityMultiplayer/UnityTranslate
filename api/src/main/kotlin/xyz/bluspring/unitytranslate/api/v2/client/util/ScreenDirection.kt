package xyz.bluspring.unitytranslate.api.v2.client.util

enum class ScreenDirection {
    UP, DOWN, LEFT, RIGHT;

    val axis: ScreenAxis
        get() = when (this) {
            UP, DOWN -> ScreenAxis.VERTICAL
            LEFT, RIGHT -> ScreenAxis.HORIZONTAL
        }

    val opposite: ScreenDirection
        get() = when(this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }

    val isPositive: Boolean
        get() = when (this) {
            UP, LEFT -> false
            DOWN, RIGHT -> true
        }

    fun isAfter(a: Int, b: Int): Boolean = if (this.isPositive) a > b else b > a
    fun isBefore(a: Int, b: Int): Boolean = if (this.isPositive) a < b else b < a
}
