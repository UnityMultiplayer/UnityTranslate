package xyz.bluspring.unitytranslate.gui.visual

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.coerceAtMost
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.events.UIClickEvent
import gg.essential.universal.UMatrixStack
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui
import xyz.bluspring.unitytranslate.gui.TranscriptBox
import xyz.bluspring.unitytranslate.gui.config.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers
import xyz.bluspring.unitytranslate.gui.elementa.constraints.WindowAwarePositionConstraint
import xyz.bluspring.unitytranslate.gui.elementa.elements.UIButton
import xyz.bluspring.unitytranslate.gui.visual.screens.UTConfigScreen
import java.awt.Color

class ContextMenuRenderer {
    private val CONTEXT_MENU_BACKGROUND = Color(20, 20, 20, 200)

    val window = Window(ElementaVersion.V10)

    private var isOpened = false
    private var focusedBox: TranscriptBox? = null

    private val transcriptBoxContextMenu = contextMenu {
        contextButton("Language: ERROR") {}
        contextButton("Text Scale: 100%") {}
        contextButton("Background Color: []") {}
        contextButton("Outline Color: []") {}
        contextButton("Outline Opacity: 39%") {}
        contextButton("Header Type: Short Language") {}
        spacing
        contextButton("Duplicate") {}
        contextButton("Remove", Color.RED) {
            UnityTranslateGui.clientConfig.transcriptBoxes.remove(focusedBox!!.config)
            UnityTranslateGui.updateConfig()
            closeContextMenu()
        }
    }

    private val mainContextMenu = contextMenu {
        contextButton("Add Transcript Box") {
            UnityTranslateGui.clientConfig.transcriptBoxes.add(UnityTranslateClientConfig.TranscriptBoxConfig(
                language = UnityTranslateGui.clientConfig.spokenLanguage,
            ))
            UnityTranslateGui.updateConfig()
            closeContextMenu()
        }
        contextButton("Open Config Screen") {
            LayeredScreenManager.open(UTConfigScreen())
            closeContextMenu()
        }
        spacing
        contextButton("Remove All", Color.RED) {
            closeContextMenu()
        }
    }

    private fun contextMenu(action: UIComponent.() -> Unit): UIComponent {
        return UIContainer().constrain {
            width = 100.pixels
            height = ChildBasedSizeConstraint(4f).coerceAtMost(FillConstraint())
            color = CONTEXT_MENU_BACKGROUND.constraint
        }.apply {
            this.isFloating = true
            action.invoke(this)
        } childOf window
    }

    private val UIComponent.spacing: UIComponent
        get() {
            return UIContainer().constrain {
                y = SiblingConstraint(4f)
                width = 100.percent
                height = 5.pixels
            } childOf this
        }

    private fun UIComponent.contextButton(text: String, color: Color = ElementaUIHelpers.TEXT_COLOR, clickAction: UIButton.(UIClickEvent) -> Unit): UIButton {
        return UIButton(text, textColor = color).constrain {
            y = SiblingConstraint(4f)
            width = 100.percent
            height = 14.pixels
        }.onClick {
            clickAction.invoke(this, it)
        } childOf this
    }

    fun onMouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isOpened) {
            if (window.hoveredFloatingComponent == null) {
                if (button == 0) { // Close context menu if clicked outside the context menu
                    closeContextMenu()
                } else if (button == 1) { // Open a different context menu if we have to
                    tryOpenContextMenu(mouseX, mouseY)
                }
            }

            window.mouseClick(mouseX, mouseY, button)

            return true
        }

        if (button == 1) { // Open on right click
            tryOpenContextMenu(mouseX, mouseY)

            return true
        }

        return false
    }

    fun closeContextMenu() {
        focusedBox = null
        isOpened = false

        transcriptBoxContextMenu.hide()
        mainContextMenu.hide()
    }

    private fun tryOpenContextMenu(mouseX: Double, mouseY: Double) {
        isOpened = true

        focusedBox = null

        // Select first hovered transcript box
        for (transcriptBox in UnityTranslateGui.transcriptRenderer.renderedBoxes.asReversed()) {
            if (transcriptBox.isPointInside(mouseX.toFloat(), mouseY.toFloat())) {
                focusedBox = transcriptBox
                break
            }
        }

        // If we don't have a transcript box, let's open our menu.
        if (focusedBox == null) {
            transcriptBoxContextMenu.hide()
            mainContextMenu.hide(true)
            mainContextMenu.unhide(false)
            calculateContextMenuPosition(mainContextMenu, mouseX, mouseY)
        } else { // Otherwise, we open the menu for the transcript box.
            // Setup specific settings for this transcript box
            (transcriptBoxContextMenu.children[0] as UIButton).text = "Language: ${focusedBox!!.config.language}"

            mainContextMenu.hide()
            transcriptBoxContextMenu.hide(true)
            transcriptBoxContextMenu.unhide(false)
            calculateContextMenuPosition(transcriptBoxContextMenu, mouseX, mouseY)
        }
    }

    // Calculates where the context box should be positioned on the user's screen
    fun calculateContextMenuPosition(contextBox: UIComponent, mouseX: Double, mouseY: Double) {
        /*if (mouseX + contextBox.getWidth() > window.getWidth()) {
            // Position context box to the left of the cursor.
            contextBox.setX(mouseX.pixels - 100.percent)
        } else if (mouseX - contextBox.getWidth() > 0) {
            // Position context box to the right of the cursor
            contextBox.setX(mouseX.pixels)
        }*/

        contextBox.setX(WindowAwarePositionConstraint(mouseX.toFloat(), 6f))
        contextBox.setY(WindowAwarePositionConstraint(mouseY.toFloat(), 0f))
    }

    fun render(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, tickDelta: Float) {
        if (isOpened) {
            window.draw(matrixStack)
        }
    }
}