package xyz.bluspring.unitytranslate.gui.menu.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.OutlineEffect.Side
import gg.essential.elementa.events.UIClickEvent
import gg.essential.universal.ChatColor
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMouse
import gg.essential.universal.UScreen
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui
import xyz.bluspring.unitytranslate.gui.config.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.gui.TranscriptBox
import xyz.bluspring.unitytranslate.gui.elementa.ColorSelector
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.button
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.cycleButton
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.slider
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.withScrollbar
import xyz.bluspring.unitytranslate.gui.menu.LayeredScreenManager
import java.awt.Color
import kotlin.math.floor

class EditTranscriptBoxesScreen : WindowScreen(ElementaVersion.V10) {
    val background = UIBlock(ElementaUIHelpers.BACKGROUND_COLOR).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 100.percentOfWindow
        this.height = 100.percentOfWindow
    } childOf window

    val doneSection = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 100.percentOfWindow - 32.pixels
        this.width = 70.percentOfWindow
        this.height = 20.pixels
    }.apply {
        button("Save")
            .constrain {
                this.x = CenterConstraint()
                this.width = 20.percent
            }
            .onMouseClick {
                LayeredScreenManager.close()
            } childOf this

        button("+")
            .constrain {
                this.x = CenterConstraint() + 10.percent + 20.pixels
                this.width = 18.pixels
            }
            .onMouseClick {
                LayeredScreenManager.open(LanguageSelectScreen {
                    UnityTranslateGui.clientConfig.transcriptBoxes.add(UnityTranslateClientConfig.TranscriptBoxConfig(it))
                    UnityTranslateGui.updateConfig()
                })
            } childOf this
    } childOf window

    private var dragRelativeX = 0f
    private var dragRelativeY = 0f
    private var moveState = MoveState()
    private var currentHoverEffect: OutlineEffect? = null

    private val hoverTranscriptBox: UIComponent.() -> Unit = {
        val mouseX = UMouse.Scaled.x
        val mouseY = UMouse.Scaled.y
        val relativeX = mouseX - this.getLeft()
        val relativeY = mouseY - this.getTop()

        moveState.left = relativeX >= this.getLeft() - 4f && relativeX <= this.getLeft() + 4f
        moveState.right = relativeX >= this.getRight() - 4f && relativeX <= this.getRight() + 4f
        moveState.top = relativeY >= this.getTop() - 4f && relativeY <= this.getTop() + 4f
        moveState.bottom = relativeY >= this.getBottom() - 4f && relativeY <= this.getBottom() + 4f

        if (moveState.isAllFalse() && this.isPointInside(mouseX.toFloat(), mouseY.toFloat())) {
            moveState.setAllTrue()
        }

        if (currentHoverEffect != null) {
            this.removeEffect(currentHoverEffect!!)
            currentHoverEffect = null
        }

        if (!moveState.isAllFalse()) {
            currentHoverEffect = moveState.createOutline()
            this.effect(currentHoverEffect!!)
        }
    }

    private val dropdownOpener: UIComponent.(UIClickEvent) -> Unit = { event ->
        if (this is TranscriptBox) {
            if (event.mouseButton == 1)
                createSettingDropdown(this, event.absoluteX, event.absoluteY)
            else if (event.mouseButton == 0) {
                dragRelativeX = event.relativeX / this.getWidth()
                dragRelativeY = event.relativeY / this.getHeight()

                moveState.left = event.relativeX >= this.getLeft() - 4f && event.relativeX <= this.getLeft() + 4f
                moveState.right = event.relativeX >= this.getRight() - 4f && event.relativeX <= this.getRight() + 4f
                moveState.top = event.relativeY >= this.getTop() - 4f && event.relativeY <= this.getTop() + 4f
                moveState.bottom = event.relativeY >= this.getBottom() - 4f && event.relativeY <= this.getBottom() + 4f

                if (moveState.isAllFalse())
                    moveState.setAllTrue()
            }
        }
    }

    private val dragTranscriptBox: UIComponent.(Float, Float, Int) -> Unit = { mouseX, mouseY, mouseButton ->
        if (this is TranscriptBox && mouseButton == 0) {
            val relativeX = (mouseX - (dragRelativeX * this.getWidth()))
            val relativeY = (mouseY - (dragRelativeY * this.getHeight()))

            val normalizedX = relativeX / window.getWidth()
            val normalizedY = relativeY / window.getHeight()

            val horizontalAlignType = if (normalizedX < 0.25)
                UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE
            else if (normalizedX > 0.75)
                UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE
            else
                UnityTranslateClientConfig.HorizontalAlignType.CENTER

            val verticalAlignType = if (normalizedY < 0.25)
                UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE
            else if (normalizedY > 0.75)
                UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE
            else
                UnityTranslateClientConfig.VerticalAlignType.CENTER

            val width25 = window.getWidth() * 0.25
            val width50 = window.getWidth() * 0.50
            val width75 = window.getWidth() * 0.75
            val height25 = window.getHeight() * 0.25
            val height50 = window.getHeight() * 0.50
            val height75 = window.getHeight() * 0.75

            val alignCorrectedX = when (horizontalAlignType) {
                UnityTranslateClientConfig.HorizontalAlignType.LEFT_EDGE ->
                    mouseX / width25
                UnityTranslateClientConfig.HorizontalAlignType.CENTER ->
                    (mouseX - width25) / width50
                UnityTranslateClientConfig.HorizontalAlignType.RIGHT_EDGE ->
                    (mouseX - width75) / width25
            }.toFloat()

            val alignCorrectedY = when (verticalAlignType) {
                UnityTranslateClientConfig.VerticalAlignType.TOP_EDGE ->
                    mouseY / height25
                UnityTranslateClientConfig.VerticalAlignType.CENTER ->
                    (mouseY - height25) / height50
                UnityTranslateClientConfig.VerticalAlignType.BOTTOM_EDGE ->
                    (mouseY - height75) / height25
            }.toFloat()

            if (moveState.isAllTrue()) { // Regular moving
                this.config.offsetX = alignCorrectedX
                this.config.offsetY = alignCorrectedY
            } else {
                if (moveState.left) {
                    val right = this.getRight()

                    this.config.offsetX = alignCorrectedX
                    this.config.width = (right - relativeX).toInt()
                }

                if (moveState.right) {
                    val left = this.getLeft()
                    this.config.width = (left - mouseX).toInt()
                }

                if (moveState.top) {
                    val bottom = this.getBottom()

                    this.config.offsetY = alignCorrectedY
                    this.config.height = (bottom - relativeY).toInt()
                }

                if (moveState.bottom) {
                    val bottom = this.getBottom()
                    this.config.height = (bottom - mouseY).toInt()
                }
            }

            this.update()
        }
    }

    var currentDropdown: UIComponent? = null

    init {
        if (!UnityTranslateGui.clientConfig.backgroundEnabled) {
            background.hide(true)
        }

        // Automatically close the dropdown if we click outside of the region.
        window.onMouseClick { event ->
            if (currentDropdown != null && !currentDropdown!!.isPointInside(event.absoluteX, event.absoluteY)) {
                currentDropdown!!.hide()
                window.removeChild(currentDropdown!!)
                currentDropdown = null
            }
        }
    }

    override fun initScreen(width: Int, height: Int) {
        super.initScreen(width, height)

        for (box in UnityTranslateGui.transcriptRenderer.renderedBoxes) {
            box.onMouseClick(dropdownOpener)
            box.onMouseDrag(dragTranscriptBox)
            box.onMouseEnter(hoverTranscriptBox)
            box.onMouseLeave(hoverTranscriptBox)
        }
    }

    fun createSettingDropdown(box: TranscriptBox, mouseX: Float, mouseY: Float) {
        val dropdownWidth = 100
        val dropdownHeight = 175
        val screenWidth = window.getWidth()
        val screenHeight = window.getHeight()

        val dropdown = UIContainer().constrain {
            this.x = if (mouseX + dropdownWidth >= screenWidth)
                ((mouseX - (screenWidth - dropdownWidth)) / screenWidth).percent
            else
                (mouseX / screenWidth).percent

            this.y = if (mouseY + dropdownHeight >= screenHeight)
                ((mouseY - (screenHeight - dropdownHeight)) / screenHeight).percent
            else
                (mouseY / screenHeight).percent

            this.width = dropdownWidth.pixels
            this.height = dropdownHeight.pixels
        } childOf window

        val scrollContainer = ScrollComponent(innerPadding = 1f).constrain {
            this.x = 0.pixels
            this.y = 0.pixels
            this.width = 100.percent
            this.height = 100.percent
        } childOf dropdown

        scrollContainer.withScrollbar()

        fun <T : UIComponent> T.defaultConstraints(): T {
            return this.constrain {
                this.x = CenterConstraint()
                this.y = SiblingConstraint() + 4.pixels
                this.width = 100.percent
                this.height = 18.pixels
            }
        }

        // and now it's time to actually add the buttons
        slider(10, 300, box.config.textScale) {
            box.config.textScale = it
            box.update()
            "Text Scale: $it%"
        }.defaultConstraints() childOf scrollContainer

        cycleButton("Header Type", UnityTranslateClientConfig.HeaderType.entries, box.config.headerType) {
            box.config.headerType = it
            box.update()
        }.defaultConstraints() childOf scrollContainer

        ColorSelector("Box Color", Color(box.config.color)) {
            box.config.color = it.rgb
            box.update()
        }

        slider(0, 255, box.config.opacity) {
            box.config.opacity = it
            box.update()
            "Box Opacity: ${"%.2f".format((it / 255f) * 100f)}%"
        }.defaultConstraints() childOf scrollContainer

        slider(0f, 32f, box.config.radius) {
            box.config.radius = it
            box.update()
            "Box Roundness: ${"%.2f".format(it)}px"
        }.defaultConstraints() childOf scrollContainer

        slider(0f, 32f, box.config.outlineThickness) {
            box.config.outlineThickness = it
            box.update()
            "Outline Thickness: ${"%.2f".format(it)}px"
        }.defaultConstraints() childOf scrollContainer

        ColorSelector("Outline Color", Color(box.config.outlineColor)) {
            box.config.outlineColor = it.rgb
            box.update()
        }

        slider(0, 255, box.config.outlineOpacity) {
            box.config.outlineOpacity = it
            box.update()
            "Outline Opacity: ${"%.2f".format((it / 255f) * 100f)}%"
        }.defaultConstraints() childOf scrollContainer

        button("${ChatColor.RED}Remove")
            .onMouseClick {
                UnityTranslateGui.clientConfig.transcriptBoxes.remove(box.config)
                UnityTranslateGui.updateConfig()

                displayScreen(this@EditTranscriptBoxesScreen)
            }.defaultConstraints() childOf scrollContainer

        this.currentDropdown = dropdown
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        UnityTranslateGui.transcriptRenderer.render(matrixStack, partialTicks)
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        super.onMouseClicked(mouseX, mouseY, mouseButton)

        val (adjustedMouseX, adjustedMouseY) =
            if ((mouseX == floor(mouseX) && mouseY == floor(mouseY))) {
                val x = UMouse.Scaled.x
                val y = UMouse.Scaled.y

                mouseX + (x - floor(x)) to mouseY + (y - floor(y))
            } else {
                mouseX to mouseY
            }

        UnityTranslateGui.transcriptRenderer.window.mouseClick(adjustedMouseX, adjustedMouseY, mouseButton)
    }

    override fun onMouseReleased(mouseX: Double, mouseY: Double, state: Int) {
        super.onMouseReleased(mouseX, mouseY, state)
        UnityTranslateGui.transcriptRenderer.window.mouseRelease()
    }

    override fun onScreenClose() {
        super.onScreenClose()

        UnityTranslateGui.saveConfig()
        UnityTranslateGui.updateConfig()

        for (box in UnityTranslateGui.transcriptRenderer.renderedBoxes) {
            box.mouseClickListeners.remove(dropdownOpener)
            box.mouseDragListeners.remove(dragTranscriptBox)
        }

        LayeredScreenManager.close()
    }

    private data class MoveState(var top: Boolean = false, var bottom: Boolean = false, var left: Boolean = false, var right: Boolean = false) {
        fun isAllTrue(): Boolean {
            return top && bottom && left && right
        }

        fun isAllFalse(): Boolean {
            return !top && !bottom && !left && !right
        }

        fun setAllTrue() {
            top = true
            bottom = true
            left = true
            right = true
        }

        fun createOutline(): OutlineEffect {
            val sides = mutableSetOf(Side.Left, Side.Top, Side.Right, Side.Bottom)

            if (!top) sides.remove(Side.Top)
            if (!bottom) sides.remove(Side.Bottom)
            if (!left) sides.remove(Side.Left)
            if (!right) sides.remove(Side.Right)

            return OutlineEffect(Color.WHITE, 2f, sides = sides)
        }
    }
}