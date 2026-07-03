package xyz.bluspring.unitytranslate.client.gui.element

import kotlinx.coroutines.*
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.util.Mth
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.multiplyAlpha
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import kotlin.math.floor
import kotlin.reflect.KMutableProperty

class DropdownList<E : Comparable<E>>(
    val x: Float, val y: Float,
    val width: Float, val height: Float,

    val font: FontReference,
    elements: suspend () -> Collection<E>,
    visualizer: (E) -> Component,

    val property: KMutableProperty<E?>,
    val type: Type,
) : UIElement() {
    var opacity = 1f

    constructor(x: Float, y: Float, width: Float, height: Float, font: FontReference, elements: suspend () -> Collection<E>, visualizer: (E) -> Component, property: KMutableProperty<E>)
        : this(x, y, width, height, font, elements, visualizer, property as KMutableProperty<E?>, Type.REQUIRED)

    enum class Type {
        REQUIRED, DEFAULTED, OPTIONAL
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default)
    }

    private val visualizer: (E?) -> Component = {
        if (it == null)
            Component.translatable("unitytranslate.dropdown.${when (this.type) {
                Type.OPTIONAL -> "none"
                Type.DEFAULTED -> "default"
                else -> throw IllegalStateException()
            }}")
        else visualizer(it)
    }

    private val elementGetter: Deferred<List<E>> = scope.async {
        elements().toList().sorted()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val elements: List<E?>
        get() {
            if (!this.elementGetter.isCompleted)
                return emptyList()

            val completed = this.elementGetter.getCompleted()
            return if (this.type != Type.REQUIRED)
                (completed.toMutableList() as MutableList<E?>).apply {
                    addFirst(null)
                }
            else
                completed
        }
    private var currentIndex = 0
    private var lastStoredIndex = 0
    private var isOpened = false
    private var scrollOffset = 0.0

    val selected: E?
        get() {
            return this.elements[this.currentIndex]
        }

    private val mainBounds = ScreenRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return this.mainBounds
    }

    private var currentTick = 0
    private val maxScrollTick = 60
    private var isReversing = false

    private var isLoaded = false

    override fun tick() {
        super.tick()

        if (!this.isReversing) {
            if (this.currentTick++ >= this.maxScrollTick) {
                this.isReversing = true
            }
        } else {
            if (this.currentTick-- <= 0) {
                this.isReversing = false
            }
        }

        if (!this.isLoaded) {
            val value = this.property.getter.call()
            if (value == null && this.type != Type.REQUIRED) {
                this.currentIndex = 0
                this.lastStoredIndex = 0
            } else {
                this.currentIndex = this.elements.indexOf(value)
                this.lastStoredIndex = this.elements.indexOf(value)
            }

            this.isLoaded = true
        }
    }

    private fun updateProperty() {
        val element = this.elements[this.currentIndex]
        this.property.setter.call(element)
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        val isHovered = this.bounds().containsPoint(mouseX, mouseY)
        val colorWithHover = if (isHovered || this.isOpened)
            -1
        else
            ARGBHelper.color(185, 255, 255, 255)

//        graphics.outline(x, y, x + width, y + height, 1f, -1)
        graphics.fill(x, y, x + width, y + height, 0, ARGBHelper.colorFromFloat(0.35f, 0f, 0f, 0f).multiplyAlpha(this.opacity))
        graphics.fill(x, y + height, x + width, y + height + 1, colorWithHover.multiplyAlpha(this.opacity)) // underline

        val disabledColor = ARGBHelper.color(255, 190, 190, 190)
        val isDisabled = !this.elementGetter.isCompleted || this.elements.isEmpty()

        if (!this.elementGetter.isCompleted) {
            graphics.text(this.font, Component.translatable("unitytranslate.dropdown.loading").append(".".repeat(floor(this.currentTick / 20f).toInt() + 1)), this.x + 4, this.y + (this.height / 2f - 4), disabledColor.multiplyAlpha(this.opacity), true)
        } else if (this.elements.isEmpty()) {
            graphics.text(this.font, Component.translatable("unitytranslate.dropdown.empty"), this.x + 4, this.y + (this.height / 2f - 4), disabledColor.multiplyAlpha(this.opacity), true)
        } else {
            graphics.text(this.font, ellipsize(this.visualizer(this.selected), this.width.toInt() - 15), this.x + 4, this.y + (this.height / 2f - 4), colorWithHover.multiplyAlpha(this.opacity), true)
        }

        graphics.text(this.font, Component.literal(if (this.isOpened) "▲" else "▼"), this.x + this.width - 10, this.y + (this.height / 2f - 4), (if (isDisabled) disabledColor else colorWithHover).multiplyAlpha(this.opacity), true)
    }

    private fun ellipsize(text: FormattedText, maxWidth: Int): FormattedText {
        val subbed = this.font.substr(text, maxWidth)

        return if (subbed != text)
            Component.literal("${subbed.string}...")
        else
            text
    }

    override fun submitLate(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submitLate(graphics, partialTick, mouseX, mouseY)

        if (this.isOpened) {
            val elements = this.elements
            val usableScreenHeight = ClientPlatformProxy.instance.viewportHeight - this.y - this.height - 2
            val elementHeight = (this.height * elements.size)
            val maxAreaHeight = elementHeight.coerceAtMost(usableScreenHeight - 4)

            val yStart = (this.y + this.height + 2)

            val shouldShowScroll = maxAreaHeight != elementHeight

            graphics.fill(this.x, yStart, this.x + this.width, yStart + maxAreaHeight,
                ARGBHelper.colorFromFloat(0.9f, 0f, 0f, 0f).multiplyAlpha(this.opacity))

            graphics.enableScissor(this.x.toInt(), yStart.toInt() + 1, (this.x + this.width).toInt(), maxAreaHeight.toInt() - 2)
            graphics.pushMatrix()
            graphics.translate(0f, this.scrollOffset.toFloat())

            val mouseY = mouseY - this.scrollOffset.toFloat()

            for (i in elements.indices) {
                val y = yStart + (i * this.height)

                val isHovered = mouseX >= this.x && mouseY >= y && mouseX <= (this.x + this.width) && mouseY <= (y + this.height)
                val colorWithHover = if (isHovered)
                    -1
                else if (this.selected == elements[i])
                    ARGBHelper.color(255, 255, 255, 0)
                else
                    ARGBHelper.color(255, 185, 185, 185)

                val text = this.visualizer(elements[i])
                val maxWidth = this.width.toInt() - 15

                if (isHovered) {
                    graphics.pushMatrix()

                    val textWidth = font.width(text)
                    if (textWidth > maxWidth) {
                        graphics.enableScissor(this.x.toInt() + 4, y.toInt(), maxWidth, this.height.toInt())
                        graphics.translate(Mth.lerp((this.currentTick + (partialTick * (if (this.isReversing) -1f else 1f))) / this.maxScrollTick.toFloat(), 0f, (maxWidth - textWidth).toFloat()), 0f)
                    }

                    graphics.text(this.font, text, this.x + 4, y + 4f, colorWithHover.multiplyAlpha(this.opacity), true)

                    if (textWidth > maxWidth) {
                        graphics.disableScissor()
                    }

                    graphics.popMatrix()
                } else {
                    graphics.text(this.font, ellipsize(text, maxWidth), this.x + 4, y + 4f, colorWithHover.multiplyAlpha(this.opacity), true)
                }
            }

            graphics.popMatrix()
            graphics.disableScissor()

            graphics.outline(this.x, this.y + this.height + 2, this.x + this.width, this.y + this.height + 2 + maxAreaHeight, 1f,
                ARGBHelper.colorFromFloat(0.1f, 1f, 1f, 1f).multiplyAlpha(this.opacity))

            if (shouldShowScroll) {
                val elementsPerHeight = maxAreaHeight / this.height
                val scrollHeight = elementsPerHeight / elements.size

                val scrollYOffset = (this.scrollOffset.toFloat() / elementHeight) * maxAreaHeight
                graphics.fill(this.x + this.width - 1, yStart - scrollYOffset + 1, this.x + this.width, yStart - scrollYOffset + (maxAreaHeight * scrollHeight) - 1, (-1).multiplyAlpha(this.opacity))
            }
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val elements = this.elements
        val usableScreenHeight = ClientPlatformProxy.instance.viewportHeight - this.y - this.height - 2
        val elementHeight = (this.height * elements.size)
        val maxAreaHeight = elementHeight.coerceAtMost(usableScreenHeight - 4)

        if (this.isOpened && mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y + height + 2 && mouseY <= this.y + this.height + 2 + maxAreaHeight) {
            this.scrollOffset = Mth.clamp(scrollOffset + (scrollY * 5.0), (-elementHeight + maxAreaHeight).toDouble(), 0.0)
            return true
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.mainBounds.containsPoint(mouseX.toInt(), mouseY.toInt()) && this.isLoaded && this.elements.isNotEmpty()) {
            this.isOpened = !this.isOpened
            this.lastStoredIndex = this.currentIndex
        } else if (this.isOpened) {
            val elements = this.elements
            val usableScreenHeight = ClientPlatformProxy.instance.viewportHeight - this.y - this.height - 2
            val elementHeight = (this.height * elements.size)
            val maxAreaHeight = elementHeight.coerceAtMost(usableScreenHeight - 4)

            if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y + height + 2 && mouseY <= this.y + this.height + 2 + maxAreaHeight) {
                val mouseY = mouseY - this.scrollOffset.toFloat()
                val yOffset = mouseY - (this.y + height + 2)
                val index = Mth.clamp(((yOffset / elementHeight * elements.size)).toInt(), 0, elements.lastIndex)

                this.currentIndex = index
                this.isOpened = false
                this.updateProperty()

                return true
            }

            this.isOpened = false
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        if (this.isOpened) {
            val elements = this.elements
            val usableScreenHeight = ClientPlatformProxy.instance.viewportHeight - this.y - this.height - 2
            val elementHeight = (this.height * elements.size)
            val maxAreaHeight = elementHeight.coerceAtMost(usableScreenHeight - 4)

            val topVisibleArea = this.y + this.height + 2 - this.scrollOffset
            val bottomVisibleArea = topVisibleArea + maxAreaHeight

            when (key) {
                GLFW.GLFW_KEY_UP -> {
                    this.currentIndex--

                    if (this.currentIndex < 0)
                        this.currentIndex = this.elements.lastIndex

                    // key up moving the scroll box
                    // FIXME: balloon languages hate this for some reason.
                    val yPos = this.y + this.height + 2 + (this.currentIndex * this.height)
                    if (yPos !in topVisibleArea..bottomVisibleArea) {
                        this.scrollOffset = Mth.clamp((maxAreaHeight - yPos).toDouble(), (-elementHeight + maxAreaHeight).toDouble(), 0.0)
                    }

                    return true
                }

                GLFW.GLFW_KEY_DOWN -> {
                    this.currentIndex++

                    if (this.currentIndex > this.elements.lastIndex)
                        this.currentIndex = 0

                    // key down moving the scroll box
                    val yPos = this.y + this.height + 2 + (this.currentIndex * this.height) + this.height
                    if (yPos !in topVisibleArea..bottomVisibleArea) {
                        this.scrollOffset = Mth.clamp((maxAreaHeight - yPos + maxAreaHeight).toDouble(), (-elementHeight + maxAreaHeight).toDouble(), 0.0)
                    }

                    return true
                }

                GLFW.GLFW_KEY_ESCAPE -> {
                    this.isOpened = false
                    this.currentIndex = this.lastStoredIndex
                    return true
                }

                GLFW.GLFW_KEY_ENTER -> {
                    this.isOpened = false
                    this.updateProperty()
                    return true
                }
            }
        }

        return super.keyPressed(key, scanCode, modifiers)
    }
}
