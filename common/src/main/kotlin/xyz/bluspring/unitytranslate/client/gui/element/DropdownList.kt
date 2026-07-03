package xyz.bluspring.unitytranslate.client.gui.element

import kotlinx.coroutines.*
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import kotlin.math.floor
import kotlin.reflect.KMutableProperty

class DropdownList<E>(
    val x: Float, val y: Float,
    val width: Float, val height: Float,

    val font: FontReference,
    elements: suspend () -> Collection<E>,
    visualizer: (E) -> Component,

    val property: KMutableProperty<E?>,
    val type: Type
) : UIElement() {
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
        elements().toList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val elements: List<E>
        get() {
            if (!this.elementGetter.isCompleted)
                return emptyList()

            return this.elementGetter.getCompleted()
        }
    private var currentIndex = 0
    private var isOpened = false

    private val indexOffset = if (this.type != Type.REQUIRED) 1 else 0

    val selected: E?
        get() {
            if (this.type != Type.REQUIRED && this.currentIndex == 0)
                return null

            return this.elements[this.currentIndex - this.indexOffset]
        }

    private val mainBounds = ScreenRectangle(x.toInt(), y.toInt(), width.toInt(), height.toInt())

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        return this.mainBounds
    }

    private var currentTick = 0
    private var isLoaded = false

    override fun tick() {
        super.tick()
        this.currentTick++

        if (this.currentTick >= 60)
            this.currentTick = 0

        if (!this.isLoaded) {
            val value = this.property.getter.call()
            if (value == null && this.type != Type.REQUIRED)
                this.currentIndex = 0
            else
                this.currentIndex = this.elements.indexOf(value)

            this.isLoaded = true
        }
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

//        graphics.outline(x, y, x + width, y + height, 1f, -1)
        graphics.fill(x, y + height, x + width, y + height + 1, -1) // underline

        val disabledColor = ARGBHelper.color(255, 190, 190, 190)
        val isDisabled = !this.elementGetter.isCompleted || this.elements.isEmpty()

        if (!this.elementGetter.isCompleted) {
            graphics.text(this.font, Component.translatable("unitytranslate.dropdown.loading").append(".".repeat(floor(this.currentTick / 20f).toInt() + 1)), this.x + 2, this.y + (this.height / 2f - 4), disabledColor, true)
        } else if (this.elements.isEmpty()) {
            graphics.text(this.font, Component.translatable("unitytranslate.dropdown.empty"), this.x + 2, this.y + (this.height / 2f - 4), disabledColor, true)
        } else {
            graphics.text(this.font, this.font.split(this.visualizer(this.selected), this.width.toInt() - 30)[0], this.x + 2, this.y + (this.height / 2f - 4), -1, true)
        }

        graphics.text(this.font, Component.literal(if (this.isOpened) "▲" else "▼"), this.x + this.width - 10, this.y + (this.height / 2f - 4), if (isDisabled) disabledColor else -1, true)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.mainBounds.containsPoint(mouseX.toInt(), mouseY.toInt())) {
            this.isOpened = !this.isOpened
            return true
        } else if (this.isOpened) {
            this.isOpened = false
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun keyPressed(key: Int, scanCode: Int, modifiers: Int): Boolean {
        if (this.isOpened) {
            if (key == GLFW.GLFW_KEY_UP) {
                this.currentIndex--

                if (this.currentIndex < 0)
                    this.currentIndex = this.elements.lastIndex

                return true
            } else if (key == GLFW.GLFW_KEY_DOWN) {
                this.currentIndex++

                if (this.currentIndex > this.elements.lastIndex)
                    this.currentIndex = 0

                return true
            }
        }

        return super.keyPressed(key, scanCode, modifiers)
    }
}
