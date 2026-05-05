package gg.essential.universal


object UResolution {

    @JvmStatic
    val windowWidth: Int
        get() {
            return UMinecraft.getMinecraft().window.screenWidth
        }

    @JvmStatic
    val windowHeight: Int
        get() {
            return UMinecraft.getMinecraft().window.screenHeight
        }

    @JvmStatic
    val viewportWidth: Int
        get() {
            return UMinecraft.getMinecraft().window.width
        }

    @JvmStatic
    val viewportHeight: Int
        get() {
            return UMinecraft.getMinecraft().window.height
        }


    @JvmStatic
    val scaledWidth: Int
        get() {
            return UMinecraft.getMinecraft().window.guiScaledWidth
        }

    @JvmStatic
    val scaledHeight: Int
        get() {
            return UMinecraft.getMinecraft().window.guiScaledHeight
        }

    @JvmStatic
    val scaleFactor: Double
        get() {
            return UMinecraft.getMinecraft().window.guiScale
                .toDouble()
        }
}
