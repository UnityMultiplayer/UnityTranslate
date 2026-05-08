package gg.essential.universal

import xyz.bluspring.unitytranslate.client.ClientPlatformProxy


object UResolution {

    @JvmStatic
    val windowWidth: Int
        get() {
            return ClientPlatformProxy.instance.windowWidth
        }

    @JvmStatic
    val windowHeight: Int
        get() {
            return ClientPlatformProxy.instance.windowHeight
        }

    @JvmStatic
    val viewportWidth: Int
        get() {
            return ClientPlatformProxy.instance.viewportWidth
        }

    @JvmStatic
    val viewportHeight: Int
        get() {
            return ClientPlatformProxy.instance.viewportHeight
        }


    @JvmStatic
    val scaledWidth: Int
        get() {
            return (ClientPlatformProxy.instance.windowWidth / scaleFactor).toInt()
        }

    @JvmStatic
    val scaledHeight: Int
        get() {
            return (ClientPlatformProxy.instance.windowHeight / scaleFactor).toInt()
        }

    @JvmStatic
    val scaleFactor: Double
        get() {
            return ClientPlatformProxy.instance.guiScale
        }
}
