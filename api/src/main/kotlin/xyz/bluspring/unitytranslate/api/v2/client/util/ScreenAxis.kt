package xyz.bluspring.unitytranslate.api.v2.client.util

enum class ScreenAxis {
    HORIZONTAL, VERTICAL;

    val orthogonal: ScreenAxis
        get() = when (this) {
            HORIZONTAL -> VERTICAL
            VERTICAL -> HORIZONTAL
        }

    val positive: ScreenDirection
        get() = when (this) {
            HORIZONTAL -> ScreenDirection.RIGHT
            VERTICAL -> ScreenDirection.DOWN
        }

    val negative: ScreenDirection
        get() = when (this) {
            HORIZONTAL -> ScreenDirection.LEFT
            VERTICAL -> ScreenDirection.UP
        }

    fun getDirection(positive: Boolean): ScreenDirection
        = if (positive) this.positive else this.negative
}
