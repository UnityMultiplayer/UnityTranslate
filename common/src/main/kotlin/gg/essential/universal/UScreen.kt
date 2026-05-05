package gg.essential.universal

import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.universal.UKeyboard.toInt
import gg.essential.universal.UKeyboard.toModifiers
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo
import net.minecraft.network.chat.Component

abstract class UScreen(
    val restoreCurrentGuiOnClose: Boolean = false,
    open var newGuiScale: Int = -1,
    open var unlocalizedName: String? = null
) :
    Screen(Component.translatable(unlocalizedName ?: ""))
{
    @JvmOverloads
    constructor(
        restoreCurrentGuiOnClose: Boolean = false,
        newGuiScale: Int = -1,
    ) : this(restoreCurrentGuiOnClose, newGuiScale, null)

    private var guiScaleToRestore = -1
    private var restoringGuiScale = false
    private val screenToRestore: Screen? = if (restoreCurrentGuiOnClose) currentScreen else null
    // Background is now draw from the final `renderWithTooltip` method, before we ever get control, so we need
    // to suppress by default and can only allow during `onDrawScreen`.
    private var suppressBackground = true

    private val advancedDrawContext = AdvancedDrawContext()

    private var drawContexts = mutableListOf<GuiGraphicsExtractor>()
    private inline fun <R> withDrawContext(matrixStack: UMatrixStack, block: (GuiGraphicsExtractor) -> R) {
        val context = drawContexts.last()
        context.pose().pushMatrix()
        matrixStack.to3x2Joml(context.pose())
        block(context)
        context.pose().popMatrix()
    }

    private var lastClick = 0L
    private var lastDraggedDx = -1.0
    private var lastDraggedDy = -1.0
    private var lastScrolledX = -1.0
    private var lastScrolledY = -1.0
    private var lastScrolledDX = 0.0

    final override fun init() {
        updateGuiScale()
        initScreen(width, height)
    }

    override fun getTitle(): Component = Component.translatable(unlocalizedName ?: "")

    final override fun extractRenderState(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        drawContexts.add(context)
        advancedDrawContext.nextFrame()
        advancedDrawContext.drawImmediate(context) { stack ->
            suppressBackground = false
            onDrawScreenCompat(stack, mouseX, mouseY, delta)
            suppressBackground = true
        }
        drawContexts.removeLast()
    }

    final override fun keyPressed(input: KeyEvent): Boolean {
        onKeyPressed(input.key, 0.toChar(), input.modifiers.toModifiers())
        return false
    }

    final override fun keyReleased(input: KeyEvent): Boolean {
        onKeyReleased(input.key, 0.toChar(), input.modifiers.toModifiers())
        return false
    }

    final override fun charTyped(input: CharacterEvent): Boolean {
        val codepoint = input.codepoint
        val modifiers = 0.toModifiers()
        if (Character.isBmpCodePoint(codepoint)) {
            onKeyPressed(0, input.codepoint.toChar(), modifiers)
        } else if (Character.isValidCodePoint(codepoint)) {
            onKeyPressed(0, Character.highSurrogate(input.codepoint), modifiers)
            onKeyPressed(0, Character.lowSurrogate(input.codepoint), modifiers)
        }
        return false
    }

    private var lastMouseInput: MouseButtonInfo? = null
    private var lastDoubled: Boolean? = null

    final override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        lastMouseInput = click.buttonInfo
        lastDoubled = doubled
        if (click.button() == 1) lastClick = UMinecraft.getTime()
        onMouseClicked(click.x, click.y, click.button())
        lastMouseInput = null
        lastDoubled = null
        return false
    }

    final override fun mouseReleased(click: MouseButtonEvent): Boolean {
        lastMouseInput = click.buttonInfo
        onMouseReleased(click.x, click.y, click.button())
        lastMouseInput = null
        return false
    }

    override fun mouseDragged(click: MouseButtonEvent, offsetX: Double, offsetY: Double): Boolean {
        lastMouseInput = click.buttonInfo
        lastDraggedDx = offsetX
        lastDraggedDy = offsetY
        onMouseDragged(click.x, click.y, click.button(), UMinecraft.getTime() - lastClick)
        lastMouseInput = null
        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, delta: Double): Boolean {
        lastScrolledDX = horizontalAmount
        lastScrolledX = mouseX
        lastScrolledY = mouseY
        @Suppress("DEPRECATION")
        onMouseScrolled(delta)
        return false
    }

    final override fun tick(): Unit = onTick()

    final override fun removed() {
        advancedDrawContext.close()
        onScreenClose()
        restoreGuiScale()
    }

    private var lastBackgroundMouseX = 0
    private var lastBackgroundMouseY = 0
    private var lastBackgroundDelta = 0f
    final override fun extractBackground(context: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, delta: Float) {
        lastBackgroundMouseX = mouseX
        lastBackgroundMouseY = mouseY
        lastBackgroundDelta = delta
        if (suppressBackground) return
        drawContexts.add(context)
        onDrawBackgroundCompat(UMatrixStack(context.pose()), 0)
        drawContexts.removeLast()
    }

    constructor(restoreCurrentGuiOnClose: Boolean, newGuiScale: GuiScale) : this(
        restoreCurrentGuiOnClose,
        newGuiScale.ordinal
    )

    fun restorePreviousScreen() {
        displayScreen(screenToRestore)
    }

    open fun updateGuiScale() {
        if (newGuiScale != -1 && !restoringGuiScale) {
            if (guiScaleToRestore == -1)
                guiScaleToRestore = UMinecraft.guiScale
            UMinecraft.guiScale = newGuiScale
            width = UResolution.scaledWidth
            height = UResolution.scaledHeight
        }
    }

    private fun restoreGuiScale() {
        if (guiScaleToRestore != -1) {
            // This flag is necessary since on 1.20.5 setting the gui scale causes the screen's resize
            // method to be called due to an option change callback. This resize causes the screen to reinitialize,
            // which calls updateGuiScale. To prevent that method for changing the gui scale back,
            // we suppress its behavior with a flag.
            restoringGuiScale = true
            UMinecraft.guiScale = guiScaleToRestore
            restoringGuiScale = false
            guiScaleToRestore = -1
        }
    }

    open fun initScreen(width: Int, height: Int) {
        super.init()
    }

    open fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        withDrawContext(matrixStack) { drawContext ->
            super.extractRenderState(drawContext, mouseX, mouseY, partialTicks)
        }
    }

    @Deprecated(
        UMatrixStack.Compat.DEPRECATED,
        ReplaceWith("onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)")
    )
    open fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        onDrawScreen(UMatrixStack.Compat.get(), mouseX, mouseY, partialTicks)
    }

    // Calls the deprecated method (for backwards compat) which then calls the new method (read the deprecation message)
    private fun onDrawScreenCompat(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) = UMatrixStack.Compat.runLegacyMethod(matrixStack) {
        @Suppress("DEPRECATION")
        onDrawScreen(mouseX, mouseY, partialTicks)
    }

    open fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (keyCode != 0) {
            super.keyPressed(KeyEvent(keyCode, 0, modifiers.toInt()))
        }
        if (typedChar != 0.toChar()) {
            super.charTyped(CharacterEvent(typedChar.code))
        }
    }

    open fun onKeyReleased(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (keyCode != 0) {
            super.keyReleased(KeyEvent(keyCode, 0, modifiers.toInt()))
        }
    }

    open fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        if (mouseButton == 1)
            lastClick = UMinecraft.getTime()
        super.mouseClicked(MouseButtonEvent(mouseX, mouseY, MouseButtonInfo(mouseButton, lastMouseInput?.modifiers ?: 0)), lastDoubled ?: false)
    }

    open fun onMouseReleased(mouseX: Double, mouseY: Double, state: Int) {
        super.mouseReleased(MouseButtonEvent(mouseX, mouseY, MouseButtonInfo(state, lastMouseInput?.modifiers ?: 0)))
    }

    open fun onMouseDragged(x: Double, y: Double, clickedButton: Int, timeSinceLastClick: Long) {
        super.mouseDragged(MouseButtonEvent(x, y, MouseButtonInfo(clickedButton, lastMouseInput?.modifiers ?: 0)), lastDraggedDx, lastDraggedDy)
    }

    // This function receives the delta from both lwjgl 2 and lwjgl 3.
    // The deltas obtained from lwjgl 2 are scaled by a constant factor and thus much higher than the ones provided by lwjgl 3.
    @Deprecated("Provided `delta` values have different units depending on Minecraft versions.", ReplaceWith("onMouseScrolled(mouseX, mouseY, deltaHorizontal, deltaVertical)"))
    open fun onMouseScrolled(delta: Double) {
        onMouseScrolled(lastScrolledX, lastScrolledY, lastScrolledDX, delta)
    }

    // Must be called with consistently scaled deltas on all mc/lwjgl versions.
    // This is to ensure a consistent scrolling experience across all versions.
    // See older function above this for further explanation.
    open fun onMouseScrolled(mouseX: Double, mouseY: Double, deltaHorizontal: Double, deltaVertical: Double) {
        super.mouseScrolled(mouseX, mouseY, deltaHorizontal, deltaVertical)
    }

    open fun onTick() {
        super.tick()
    }

    open fun onScreenClose() {
        super.removed()
    }

    open fun onDrawBackground(matrixStack: UMatrixStack, tint: Int) {
        withDrawContext(matrixStack) { drawContext ->
            drawContext.nextStratum()
            val orgProjectionMatrixBuffer = RenderSystem.getProjectionMatrixBuffer()!!
            val orgProjectionType = RenderSystem.getProjectionType()
            super.extractBackground(drawContext, lastBackgroundMouseX, lastBackgroundMouseY, lastBackgroundDelta)
            RenderSystem.setProjectionMatrix(orgProjectionMatrixBuffer, orgProjectionType)
            drawContext.nextStratum()
        }
    }

    @Deprecated(
        UMatrixStack.Compat.DEPRECATED,
        ReplaceWith("onDrawBackground(matrixStack, tint)")
    )
    open fun onDrawBackground(tint: Int) {
        onDrawBackground(UMatrixStack.Compat.get(), tint)
    }

    // Calls the deprecated method (for backwards compat) which then calls the new method (read the deprecation message)
    fun onDrawBackgroundCompat(matrixStack: UMatrixStack, tint: Int) = UMatrixStack.Compat.runLegacyMethod(matrixStack) {
        @Suppress("DEPRECATION")
        onDrawBackground(tint)
    }

    companion object {
        @JvmStatic
        val currentScreen: Screen?
            get() = UMinecraft.currentScreenObj as? Screen?

        @JvmStatic
        fun displayScreen(screen: Screen?) {
            UMinecraft.currentScreenObj = screen
        }
    }
}
