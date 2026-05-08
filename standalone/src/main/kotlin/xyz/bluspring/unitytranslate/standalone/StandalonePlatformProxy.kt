package xyz.bluspring.unitytranslate.standalone

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.textures.GpuTexture
import gg.essential.universal.UMatrixStack
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.renderer.UnityTranslateElementaGui
import xyz.bluspring.unitytranslate.standalone.input.Mouse
import java.nio.file.Path
import kotlin.io.path.Path

class StandalonePlatformProxy : PlatformProxy, ClientPlatformProxy {
    override val version: String = UnityTranslateStandalone.metadata.version
    override val pluginsDir: Path = Path("plugins")
    override val nativesDir: Path = Path("natives")
    override val framebuffer: RenderTarget
        get() = UnityTranslateStandalone.framebuffer

    override fun getTexture(id: Int): GpuTexture {
        TODO("Not yet implemented")
    }

    override val renderThread: Thread
        get() = UnityTranslateStandalone.gameThread
    override val mouseX: Double
        get() = Mouse.x
    override val mouseY: Double
        get() = Mouse.y
    override val windowWidth: Int
        get() = UnityTranslateStandalone.window.width
    override val windowHeight: Int
        get() = UnityTranslateStandalone.window.height
    override val viewportWidth: Int
        get() = UnityTranslateStandalone.window.screenWidth
    override val viewportHeight: Int
        get() = UnityTranslateStandalone.window.screenHeight
    override val guiScale: Double
        get() = UnityTranslateStandalone.window.guiScale.toDouble()

    override fun renderGui(): Boolean {
        return UnityTranslateElementaGui.render(UMatrixStack(), Mouse.x.toInt(), Mouse.y.toInt(), UnityTranslateStandalone.deltaTracker.getGameTimeDeltaPartialTick(true))
    }
}
