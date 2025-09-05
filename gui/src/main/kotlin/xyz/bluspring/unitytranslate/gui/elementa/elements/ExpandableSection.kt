package xyz.bluspring.unitytranslate.gui.elementa.elements

import gg.essential.elementa.UIComponent
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
import xyz.bluspring.unitytranslate.gui.elementa.constraints.CramAwareChildBasedSizeConstraint

class ExpandableSection(text: String, builder: UIComponent.() -> Unit) : UIContainer() {
    var isExpanded = false
        private set
    private val container = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = SiblingConstraint() + 5.pixels

        this.width = 95.percent + 10.pixels
        this.height = CramAwareChildBasedSizeConstraint(0f) + 8.pixels
    }
        .apply {
            builder.invoke(this)
        }

    // Expand button
    private val button = button(text)
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

    fun setExpanded(value: Boolean): ExpandableSection {
        isExpanded = value
        arrowText.setText(if (value) UP_ARROW else DOWN_ARROW)
        if (value)
            container.unhide()
        else
            container.hide()

        return this
    }

    init {
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