package xyz.bluspring.unitytranslate.client

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.textures.GpuTexture
import java.util.*

interface ClientPlatformProxy {
    val framebuffer: RenderTarget
    fun renderGui(): Boolean
    fun getTexture(id: Int): GpuTexture
    val renderThread: Thread

    val mouseX: Double
    val mouseY: Double

    val windowWidth: Int
    val windowHeight: Int
    val viewportWidth: Int
    val viewportHeight: Int
    val guiScale: Double

    companion object {
        val instance: ClientPlatformProxy = ServiceLoader.load(ClientPlatformProxy::class.java).first()
    }
}
