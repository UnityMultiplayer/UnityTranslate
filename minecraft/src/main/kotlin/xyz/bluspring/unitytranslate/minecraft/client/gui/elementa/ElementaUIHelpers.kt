package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import net.minecraft.ChatFormatting
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.constraints.CramAwareChildBasedSizeConstraint
import java.awt.Color

val BUTTON_COLOR = Color(0x282828)
val OUTLINE_COLOR = Color.BLACK
val OUTLINE_HOVER_COLOR = Color.WHITE

fun toggleButton(text: String, current: Boolean, valueConsumer: (Boolean) -> Unit): UIComponent {
    var current = current
    var color = if (current)
        ChatFormatting.GREEN
    else
        ChatFormatting.RED
    val update = { "$text: $color${if (current) "Enabled" else "Disabled"}" }

    return button(update.invoke()) { str ->
        current = !current
        color = if (current)
            ChatFormatting.GREEN
        else
            ChatFormatting.RED
        valueConsumer.invoke(current)
        update.invoke()
    }
}

fun button(text: String, onClick: UIComponent.() -> Unit): UIComponent {
    return button(text) { str ->
        onClick.invoke(this)
        str
    }
}

fun button(text: String, onClick: UIComponent.(String) -> String = { it }): UIComponent {
    val outline = RoundedOutlineEffect(1f, OUTLINE_COLOR)
    val text = UIWrappedText(text, centered = true).constrain {
        this.x = CenterConstraint()
        this.y = CenterConstraint()
        this.width = 100.percent
    }

    return UIRoundedRectangle(4f)
        .constrain {
            this.height = 20.pixels
            this.color = BUTTON_COLOR.constraint
        }
        .effect(outline)
        .apply {
            text childOf this
        }
        .onMouseEnter {
            outline.color = OUTLINE_HOVER_COLOR
        }
        .onMouseLeave {
            outline.color = OUTLINE_COLOR
        }
        .onMouseClick {
            text.setText(onClick.invoke(this, text.getText()))
        }
}

fun expandableSection(text: String, builder: UIComponent.() -> Unit): UIComponent {
    return UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = SiblingConstraint() + 5.pixels
        this.width = 85.percentOfWindow
        this.height = ChildBasedSizeConstraint(4f)
    }.apply {
        val container = UIContainer().constrain {
            this.x = CenterConstraint()
            this.y = SiblingConstraint() + 5.pixels

            this.width = 80.percentOfWindow + 10.pixels
            this.height = CramAwareChildBasedSizeConstraint(0f) + 8.pixels
        }
            .apply {
                builder.invoke(this)
            }

        // Expand button
        button(text)
            .constrain {
                this.x = CenterConstraint()
                this.y = 4.pixels
                this.width = 85.percentOfWindow
            }
            .apply {
                val upArrow = "▲"
                val downArrow = "▼"

                val arrowText = UIText(downArrow).constrain {
                    this.x = 100.percent - 16.pixels
                    this.y = CenterConstraint()
                } childOf this

                onMouseClick {
                    if (arrowText.getText() == upArrow) {
                        arrowText.setText(downArrow)
                        container.hide()
                    } else {
                        arrowText.setText(upArrow)
                        container.unhide()
                    }
                }
            } childOf this

        // Expanded container just below
        container childOf this
        container.hide(true)
    }
}