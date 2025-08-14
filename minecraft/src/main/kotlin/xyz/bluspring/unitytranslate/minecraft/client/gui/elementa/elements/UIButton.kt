package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.elements

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
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.BUTTON_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.DISABLED_BUTTON_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.OUTLINE_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.OUTLINE_HOVER_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.TEXT_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects.RoundedOutlinedBevelEffect

class UIButton(var text: String, val onClick: UIComponent.(String) -> String = { it }) : UIRoundedRectangle(4f) {
    var isDisabled = false
        set(value) {
            field = value

            if (value)
                this.setColor(DISABLED_BUTTON_COLOR)
            else
                this.setColor(BUTTON_COLOR)
        }

    val outline = RoundedOutlinedBevelEffect(1f, OUTLINE_COLOR)
    val textElement = UIWrappedText(text, centered = true).constrain {
        this.x = CenterConstraint()
        this.y = CenterConstraint()
        this.width = 100.percent
        this.color = TEXT_COLOR.constraint
    } childOf this

    init {
        constrain {
            this.height = 16.pixels
            this.color = BUTTON_COLOR.constraint
        } effect outline

        this
            .onMouseEnter {
                if (!isDisabled)
                    outline.color = OUTLINE_HOVER_COLOR
            }
            .onMouseLeave {
                outline.color = OUTLINE_COLOR
            }
            .onMouseClick {
                if (isDisabled)
                    return@onMouseClick

                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f))
                textElement.setText(onClick.invoke(this, textElement.getText()))
            }
    }
}