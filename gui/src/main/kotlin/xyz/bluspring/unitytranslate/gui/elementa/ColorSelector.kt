package xyz.bluspring.unitytranslate.gui.elementa

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.TEXT_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.button
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.slider
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.textInput
import xyz.bluspring.unitytranslate.gui.elementa.constraints.CramAwareChildBasedSizeConstraint
import java.awt.Color
import kotlin.text.lowercase

class ColorSelector(val text: String, var currentColor: Color, val update: (Color) -> Unit) : UIContainer() {
    var isExpanded = false
        private set

    private val hexInput = textInput("Hex Value", currentColor.rgb.toHexString(HexFormat.UpperCase).padStart(6, '0'))

    private val redController = slider(0, 255, currentColor.red) {
        currentColor = Color(it, currentColor.green, currentColor.blue)
        update.invoke(currentColor)
        "Red: $it"
    }

    private val greenController = slider(0, 255, currentColor.green) {
        currentColor = Color(currentColor.red, it, currentColor.blue)
        update.invoke(currentColor)
        "Green: $it"
    }

    private val blueController = slider(0, 255, currentColor.blue) {
        currentColor = Color(currentColor.red, currentColor.green, it)
        update.invoke(currentColor)
        "Blue: $it"
    }

    private val container = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = SiblingConstraint() + 5.pixels

        this.width = 95.percent + 10.pixels
        this.height = CramAwareChildBasedSizeConstraint(0f) + 8.pixels
    }
        .apply {
            redController childOf this
            greenController childOf this
            blueController childOf this
            hexInput childOf this
        }

    // Expand button
    private val button = button("$text: #${currentColor.rgb.toHexString(HexFormat.UpperCase).padStart(6, '0')}")
        .constrain {
            this.x = CenterConstraint()
            this.y = 4.pixels
            this.width = 100.percent
        }
        .onMouseClick {
            setExpanded(!isExpanded)
        } childOf this

    private val arrowText = UIText(DOWN_ARROW).constrain {
        this.x = 100.percent - 16.pixels
        this.y = CenterConstraint()
        this.color = TEXT_COLOR.constraint
    }

    fun setExpanded(value: Boolean): ColorSelector {
        isExpanded = value
        arrowText.setText(if (value) UP_ARROW else DOWN_ARROW)
        if (value)
            container.unhide()
        else
            container.hide()

        return this
    }

    init {
        hexInput.onUpdate {
            val color = try {
                Color(it.lowercase().hexToInt())
            } catch (_: Throwable) {
                Color.BLACK
            }
            hexInput.setText(color.rgb.toHexString(HexFormat.UpperCase).padStart(6, '0'))
            this@ColorSelector.currentColor = color

            redController.update(color.red / 255f)
            greenController.update(color.green / 255f)
            blueController.update(color.blue / 255f)
        }

        arrowText childOf button
        // Expanded container just below
        container childOf this
        container.hide(true)
    }

    companion object {
        const val UP_ARROW = "▲"
        const val DOWN_ARROW = "▼"
    }
}