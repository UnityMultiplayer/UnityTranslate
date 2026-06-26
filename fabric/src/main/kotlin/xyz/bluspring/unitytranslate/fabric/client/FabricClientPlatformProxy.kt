package xyz.bluspring.unitytranslate.fabric.client

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.textures.GpuTexture
import net.minecraft.client.Minecraft
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.WrappedUTScreen
import xyz.bluspring.unitytranslate.client.gui.screen.UTScreen

class FabricClientPlatformProxy : ClientPlatformProxy {
    override val framebuffer: RenderTarget
        get() = Minecraft.getInstance().gameRenderer.mainRenderTarget()

    override val shouldRenderGui: Boolean
        get() {
            return !Minecraft.getInstance().gameRenderer.gameRenderState().guiRenderState.isHudHidden
        }

    override fun getTexture(id: Int): GpuTexture {
        TODO("Not yet implemented")
    }

    override fun setScreen(screen: UTScreen?) {
        Minecraft.getInstance().execute {
            if (screen != null) {
                Minecraft.getInstance().setScreenAndShow(WrappedUTScreen(screen))
            } else {
                Minecraft.getInstance().gui.setScreen(null)
            }
        }
    }

    override val renderThread: Thread
        get() = Minecraft.getInstance().runningThread
    override val mouseX: Double
        get() = Minecraft.getInstance().mouseHandler.xpos()
    override val mouseY: Double
        get() = Minecraft.getInstance().mouseHandler.ypos()
    override val windowWidth: Int
        get() = Minecraft.getInstance().window.width
    override val windowHeight: Int
        get() = Minecraft.getInstance().window.height
    override val viewportWidth: Int
        get() = Minecraft.getInstance().window.screenWidth
    override val viewportHeight: Int
        get() = Minecraft.getInstance().window.screenHeight
    override val guiScale: Double
        get() = Minecraft.getInstance().window.guiScale.toDouble()
}
