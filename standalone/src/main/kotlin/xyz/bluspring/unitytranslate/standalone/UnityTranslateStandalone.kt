package xyz.bluspring.unitytranslate.standalone

import com.google.gson.JsonParser
import com.mojang.blaze3d.opengl.GlBackend
import com.mojang.blaze3d.pipeline.MainTarget
import com.mojang.blaze3d.platform.*
import com.mojang.blaze3d.shaders.GpuDebugOptions
import com.mojang.blaze3d.systems.BackendCreationException
import com.mojang.blaze3d.systems.GpuDevice
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.DeltaTracker
import net.minecraft.client.FramerateLimiter
import net.minecraft.util.Util
import net.minecraft.util.profiling.Profiler
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.shared.HandledException
import xyz.bluspring.unitytranslate.shared.Metadata
import xyz.bluspring.unitytranslate.standalone.input.Mouse
import xyz.bluspring.unitytranslate.standalone.resources.ShaderManager
import xyz.bluspring.unitytranslate.standalone.resources.StandalonePackResources
import java.util.*

object UnityTranslateStandalone : WindowEventHandler {
    val metadata = Metadata.parse(JsonParser.parseString(this::class.java.getResource("/metadata.json")!!.readText()).asJsonObject)
    val window: Window
    val timeSource = RenderSystem.initBackendSystem(BackendOptions(false))
    val deltaTracker = DeltaTracker.Timer(20f, 0L) { it }
    val framebuffer: MainTarget

    var frameTimeNs: Long = 0

    private var running = true

    init {
        Thread.currentThread().name = "UnityTranslate Render Thread"
        RenderSystem.initRenderThread()
        Util.startTimerHackThread()

        var window: Window? = null
        lateinit var device: GpuDevice

        val backends = listOf(GlBackend())

        for (backend in backends) {
            try {
                window = Window(this, DisplayData(843, 600, OptionalInt.empty(), OptionalInt.empty(), false), null, "UnityTranslate", backend)
                device = window.backend().createDevice(window.handle(),
                    ShaderManager::getShader,
                    GpuDebugOptions(0, false, false)
                )
                RenderSystem.initRenderer(device)
                break
            } catch (e: BackendCreationException) {
                UnityTranslate.logger.error("Failed to create backend ${backend.name}", e)
                window?.close()
                window = null
            }
        }

        if (window == null) {
            throw IllegalStateException("No supported graphics backend was found.")
        }

        this.window = window
        this.framebuffer = MainTarget(843, 600)

        try {
            this.window.setIcon(StandalonePackResources, IconSet.RELEASE)
        } catch (e: Throwable) {
            UnityTranslate.logger.error("Couldn't set icon", e)
        }
    }

    @JvmStatic
    fun init() {
        try {
            UnityTranslate.init()

            try {
                this.runTick()
            } catch (e: OutOfMemoryError) {
                System.gc()
                UnityTranslate.logger.error("Ran out of memory!", e)

                throw e
            }
        } catch (e: Throwable) {
            throw HandledException(e)
        }
    }

    private fun runTick() {
        if (this.window.shouldClose()) {
            this.stop()
        }

        val renderStartTimer = Util.getNanos()
        val profiler = Profiler.get()
        profiler.push("update")

        this.deltaTracker.advanceRealTime(Util.getMillis())

        // Rendering
        profiler.popPush("render")


        profiler.popPush("gpuAsync")
        RenderSystem.executePendingTasks()

        // Present to frame
        profiler.popPush("present")
        this.framebuffer.blitToScreen()
        this.frameTimeNs = Util.getNanos() - renderStartTimer

        // Swap
        profiler.popPush("swapBuffers")
        RenderSystem.flipFrame(null)

        // Frame limit time
        profiler.popPush("frameLimiter")
        FramerateLimiter.limitDisplayFPS(144)
    }

    fun stop() {
        this.running = false
    }

    override fun resizeGui() {
    }

    override fun cursorEntered() {
        Mouse.cursorEntered()
    }
}