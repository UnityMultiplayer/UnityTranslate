package xyz.bluspring.unitytranslate.gui.elementa.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.events.UIClickEvent
import xyz.bluspring.unitytranslate.gui.elementa.CustomFontRenderer
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.BUTTON_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.BUTTON_HOVER_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.DISABLED_BUTTON_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.OUTLINE_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.OUTLINE_HOVER_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers.TEXT_COLOR
import xyz.bluspring.unitytranslate.gui.elementa.effects.RoundedOutlinedBevelEffect
import java.awt.Color

class UIButton(text: String,
    val buttonColor: Color = BUTTON_COLOR,
    val buttonHoverColor: Color = BUTTON_HOVER_COLOR,
    val disabledButtonColor: Color = DISABLED_BUTTON_COLOR,
    val outlineColor: Color = OUTLINE_COLOR,
    val outlineHoverColor: Color = OUTLINE_HOVER_COLOR,
    val textColor: Color = TEXT_COLOR
) : UIRoundedRectangle(4f) {
    var isDisabled = false
        set(value) {
            field = value

            if (value)
                this.setColor(disabledButtonColor)
            else
                this.setColor(buttonColor)
        }

    var text: String = text
        set(value) {
            field = value
            updateText()
        }

    val outline = RoundedOutlinedBevelEffect(1f, outlineColor)
    lateinit var textElement: UIWrappedText

    private fun updateText() {
        if (::textElement.isInitialized) {
            textElement.hide(true)
        }

        textElement = UIWrappedText(text, centered = true, shadow = false).constrain {
            this.x = CenterConstraint()
            this.y = CenterConstraint()
            this.width = 100.percent
            this.color = textColor.constraint
            this.fontProvider = CustomFontRenderer
        } childOf this
    }

    init {
        updateText()

        constrain {
            this.height = 16.pixels
            this.color = buttonColor.constraint
        } effect outline

        this.isFloating = true

        this
            .onMouseEnter {
                if (!isDisabled)
                    outline.color = outlineHoverColor

                this.setColor(buttonHoverColor)
            }
            .onMouseLeave {
                outline.color = outlineColor
                this.setColor(buttonColor)
            }
            .onMouseClick {
                if (isDisabled)
                    return@onMouseClick

                //Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f))
//                textElement.setText(onClick.invoke(this, textElement.getText()))
            }
    }

    fun onClick(event: UIButton.(UIClickEvent) -> Unit): UIButton {
        this.onMouseClick {
            if (isDisabled)
                return@onMouseClick

            if (it.mouseButton == 0) {
                event.invoke(this@UIButton, it)
            }
        }

        return this
    }
}