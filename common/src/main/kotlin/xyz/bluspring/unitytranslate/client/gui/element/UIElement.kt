package xyz.bluspring.unitytranslate.client.gui.element

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import org.jetbrains.annotations.ApiStatus
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig

abstract class UIElement {
    protected val children: List<UIElement>
        field = mutableListOf<UIElement>()

    protected var isSelected = false

    private val enableDebug: Boolean // don't optimize this into a const, this is so we can use hotswap to toggle debug display.
        get() = false
    private var isInitialized = false

    fun bounds(): ScreenRectangle {
        return this.getBounds(ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
    }

    fun getBounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        if (!this.isInitialized) {
            this.setup(ClientPlatformProxy.instance.viewportWidth, ClientPlatformProxy.instance.viewportHeight)
        }

        return this.bounds(screenWidth, screenHeight)
    }

    @ApiStatus.OverrideOnly
    protected abstract fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle

    protected open fun init(width: Int, height: Int) {
    }

    fun setup(width: Int, height: Int) {
        this.children.clear()
        this.init(width, height)

        for (element in this.children) {
            element.setup(width, height)
        }

        this.isInitialized = true
    }

    protected fun <T : UIElement> addChild(child: T): T {
        this.children.add(child)
        return child
    }

    open fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        // Debug thing for showing the element bounds.
        if (this.enableDebug) {
            val bounds = this.bounds()
            val isHovered = bounds.containsPoint(mouseX, mouseY)
            val color = if (isHovered)
                ARGBHelper.color(255, 255, 255, 255)
            else
                ARGBHelper.color(60, 255, 255, 255)

            graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, color)

            if (isHovered) {
                graphics.pushMatrix()
                graphics.translate(bounds.left().toFloat(), bounds.bottom().toFloat() + 2f)
                graphics.scale(0.5f, 0.5f)
                graphics.text(ClientPlatformProxy.instance.defaultFont, Component.literal("${this::class.simpleName} (x: ${bounds.left()}, y: ${bounds.top()}, width: ${bounds.right() - bounds.left()}, height: ${bounds.bottom() - bounds.top()})"), 0f, 0f, -1, true)
                graphics.popMatrix()
            }
        }

        for (element in this.children) {
            element.submit(graphics, partialTick, mouseX, mouseY)
        }

        this.submitLate(graphics, partialTick, mouseX, mouseY)
    }

    protected open fun submitLate(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        for (element in this.children) {
            element.submitLate(graphics, partialTick, mouseX, mouseY)
        }
    }

    open fun tick() {
        for (element in this.children) {
            element.tick()
        }
    }

    open fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        for (element in this.children) {
            if (element.keyPressed(key, scanCode, modifiers))
                return true
        }

        return false
    }

    open fun keyReleased(key: Int, scanCode: Int, modifiers: Int): Boolean {
        for (element in this.children) {
            if (element.keyReleased(key, scanCode, modifiers))
                return true
        }

        return false
    }

    open fun charTyped(codepoint: Int): Boolean {
        for (element in this.children) {
            if (element.charTyped(codepoint))
                return true
        }

        return false
    }

    open fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in this.children) {
            if (element.mouseClicked(mouseX, mouseY, button))
                return true
        }

        return false
    }

    open fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (element in this.children) {
            if (element.mouseReleased(mouseX, mouseY, button))
                return true
        }

        return false
    }

    open fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        for (element in this.children) {
            if (element.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
                return true
        }

        return false
    }

    protected fun tooltip(graphics: UIGraphics, tooltip: Component) {
        val mouseX = (ClientPlatformProxy.instance.mouseX / ClientPlatformProxy.instance.guiScale).toFloat()
        val mouseY = (ClientPlatformProxy.instance.mouseY / ClientPlatformProxy.instance.guiScale).toFloat()
        val font = ClientPlatformProxy.instance.defaultFont

        val split = font.split(tooltip, 220)
        val maxWidth = split.maxOf { font.width(it) }

        val tooltipWidth = maxWidth + 8
        var xPos = mouseX + 4f
        var yPos = mouseY - 3
        if (ClientPlatformProxy.instance.viewportWidth <= xPos + tooltipWidth)
            xPos -= (xPos + tooltipWidth) - ClientPlatformProxy.instance.viewportWidth + 4

        if (ClientPlatformProxy.instance.viewportHeight <= yPos + (split.size * font.lineHeight) + 5)
            yPos -= (yPos + (split.size * font.lineHeight) + 5) - ClientPlatformProxy.instance.viewportHeight + 4

        val bgMatrix = ColorConfig.separateMatrix(ThemeConfig.tooltipBackground)
        graphics.fill(xPos, yPos, xPos + tooltipWidth, yPos + (split.size * font.lineHeight) + 5,
            bgMatrix.topLeft, bgMatrix.topRight,
            bgMatrix.bottomRight, bgMatrix.bottomRight
        )

        for ((index, text) in split.withIndex()) {
            graphics.text(font, text, xPos + 4, yPos + 3 + (index * font.lineHeight),
                ThemeConfig.tooltipText, false)
        }
    }
}
