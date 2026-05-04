package xyz.bluspring.unitytranslate.standalone

import com.mojang.blaze3d.opengl.GlBackend
import com.mojang.blaze3d.platform.BackendOptions
import com.mojang.blaze3d.platform.DisplayData
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.platform.WindowEventHandler
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.util.Util
import xyz.bluspring.unitytranslate.UnityTranslate
import java.util.*

object UnityTranslateStandalone : WindowEventHandler {
    val window: Window
    val timeSource = RenderSystem.initBackendSystem(BackendOptions(false))

    init {
        Thread.currentThread().name = "UnityTranslate Render Thread"
        RenderSystem.initRenderThread()
        this.window = Window(this, DisplayData(843, 600, OptionalInt.empty(), OptionalInt.empty(), false), null, "UnityTranslate", GlBackend())
        Util.startTimerHackThread()
    }

    @JvmStatic
    fun init() {
        try {
            UnityTranslate.init()
            while (true) {}
        } catch (e: Throwable) {
            throw HandledException(e)
        }
    }

    override fun resizeGui() {
        TODO("Not yet implemented")
    }

    override fun cursorEntered() {
        TODO("Not yet implemented")
    }
}