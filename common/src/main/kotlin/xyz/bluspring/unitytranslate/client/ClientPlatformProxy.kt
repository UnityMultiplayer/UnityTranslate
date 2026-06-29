package xyz.bluspring.unitytranslate.client

import com.mojang.blaze3d.pipeline.RenderTarget
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen
import java.util.*

interface ClientPlatformProxy {
    val framebuffer: RenderTarget
    val shouldRenderGui: Boolean
    val renderThread: Thread
    val guiScale: Double

    val windowHandle: Long

    val mouseX: Double
    val mouseY: Double

    val windowWidth: Int
    val windowHeight: Int
    val viewportWidth: Int
    val viewportHeight: Int

    fun setScreen(screen: UTScreen?)

    companion object {
        val instance: ClientPlatformProxy = ServiceLoader.load(ClientPlatformProxy::class.java).first()
    }
}
