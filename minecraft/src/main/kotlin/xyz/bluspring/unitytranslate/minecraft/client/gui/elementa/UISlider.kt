package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.BUTTON_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.DISABLED_BUTTON_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.OUTLINE_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.OUTLINE_HOVER_COLOR
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects.RoundedOutlineEffect
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects.RoundedOutlinedBevelEffect

class UISlider(val min: Double, val max: Double, var current: Double, val updater: (Double) -> String) : UIRoundedRectangle(4f) {
    val outline = RoundedOutlineEffect(1f, OUTLINE_COLOR)
    val sliderOutline = RoundedOutlinedBevelEffect(1f, OUTLINE_COLOR)

    val text = UIWrappedText(updater.invoke(current), centered = true).constrain {
        this.x = CenterConstraint()
        this.y = CenterConstraint()
        this.width = 100.percent
    }
    val slider = UIRoundedRectangle(3f)
        .effect(sliderOutline)
        .constrain {
            this.width = 4.pixels
            this.height = 100.percent
            this.color = BUTTON_COLOR.constraint
            this.x = RelativeConstraint(((current - min) / (max - min)).toFloat())
            if (current >= max)
                this.x = 100.percent - 4.pixels
            this.y = 0.pixels
        }

    var shouldDrag = false

    init {
        this
            .constrain {
                this.height = 18.pixels
                this.color = DISABLED_BUTTON_COLOR.constraint
            }
            .effect(outline)
            .apply {
                slider childOf this
                text childOf this
            }
            .onMouseEnter {
                outline.color = OUTLINE_HOVER_COLOR
                sliderOutline.color = OUTLINE_HOVER_COLOR
            }
            .onMouseLeave {
                outline.color = OUTLINE_COLOR
                sliderOutline.color = OUTLINE_COLOR
            }
            .onMouseRelease {
                shouldDrag = false
            }
            .onMouseClick { event ->
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f))

                if (event.mouseButton == 1) {
                    this.hide()
                    shouldDrag = false

                    return@onMouseClick
                } else if (event.mouseButton != 0)
                    return@onMouseClick

                val value = (event.relativeX / this.getWidth()).coerceIn(0f, 1f)
                update(value)
                shouldDrag = true
            }
            .onMouseDrag { mouseX, mouseY, button ->
                if (!shouldDrag)
                    return@onMouseDrag

                val value = (mouseX / this.getWidth()).coerceIn(0f, 1f)
                update(value)
            }
    }

    fun update(value: Float) {
        current = min + value * (max - min)
        slider.constraints.x = RelativeConstraint(value)

        if (current >= max)
            slider.constraints.x = 100.percent - 4.pixels

        text.setText(updater.invoke(current))
    }
}