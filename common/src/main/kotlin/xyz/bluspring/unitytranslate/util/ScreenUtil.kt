package xyz.bluspring.unitytranslate.util

import org.joml.Vector2f

object ScreenUtil {
    @JvmStatic
    fun findAnchorPoint(centerPos: Vector2f, width: Float, height: Float, screenWidth: Int, screenHeight: Int): Vector2f {
        return Vector2f(centerPos).div(screenWidth - (width / 2f), screenHeight - (height / 2f))
    }
}
