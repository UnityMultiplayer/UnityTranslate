package gg.essential.universal


import kotlin.math.max

object UMouse {
    object Raw {
        @JvmStatic
        val x: Double
            get() {
                return UMinecraft.getMinecraft().mouseHandler.xpos()
            }

        @JvmStatic
        val y: Double
            get() {
                return UMinecraft.getMinecraft().mouseHandler.ypos()
            }
    }

    object Scaled {
        @JvmStatic
        val x: Double
            get() = Raw.x * UResolution.scaledWidth / max(1, UResolution.windowWidth)

        @JvmStatic
        val y: Double
            get() = Raw.y * UResolution.scaledHeight / max(1, UResolution.windowHeight)
    }

    @JvmStatic
    @Deprecated("Orientation is different between Minecraft versions.", replaceWith = ReplaceWith("UMouse.Raw.x"))
    fun getTrueX(): Double {
        return Raw.x
    }

    @JvmStatic
    @Deprecated("Orientation is different between Minecraft versions.", replaceWith = ReplaceWith("UMouse.Scaled.x"))
    @Suppress("DEPRECATION")
    fun getScaledX(): Double {
        return getTrueX() * UResolution.scaledWidth / max(1, UResolution.windowWidth)
    }

    @JvmStatic
    @Deprecated("Orientation is different between Minecraft versions.", replaceWith = ReplaceWith("UMouse.Raw.y"))
    fun getTrueY(): Double {
        return Raw.y
    }

    @JvmStatic
    @Deprecated("Orientation is different between Minecraft versions.", replaceWith = ReplaceWith("UMouse.Scaled.y"))
    @Suppress("DEPRECATION")
    fun getScaledY(): Double {
        return getTrueY() * UResolution.scaledHeight / max(1, UResolution.windowHeight)
    }
}
