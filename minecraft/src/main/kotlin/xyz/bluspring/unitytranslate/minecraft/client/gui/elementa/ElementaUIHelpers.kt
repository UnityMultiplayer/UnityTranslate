package xyz.bluspring.unitytranslate.minecraft.client.gui.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
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
import gg.essential.elementa.effects.OutlineEffect
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects.RoundedOutlineEffect
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.effects.RoundedOutlinedBevelEffect
import java.awt.Color

object ElementaUIHelpers {
    var DISABLED_BUTTON_COLOR = Color(0xFFFFFF)
    var BUTTON_COLOR = Color(0xFFFFFF)
    var TEXT_COLOR = Color(0xFFFFFF)
    var BACKGROUND_COLOR = Color(0xFFFFFF)

    val OUTLINE_COLOR = Color.BLACK
    val OUTLINE_HOVER_COLOR = Color.WHITE

    init {
        setup()
    }

    fun setup() {
        if (UnityTranslateMCClient.clientConfig.isDarkMode)
            setupDarkMode()
        else
            setupLightMode()
    }

    fun setupLightMode() {
        BUTTON_COLOR = Color(0x707070)
        DISABLED_BUTTON_COLOR = Color(0x282828)
        TEXT_COLOR = Color.WHITE
        BACKGROUND_COLOR = Color(0xBABABA) // banana
    }

    fun setupDarkMode() {
        BUTTON_COLOR = Color(0x232323)
        DISABLED_BUTTON_COLOR = Color(0x181818)
        TEXT_COLOR = Color.WHITE
        BACKGROUND_COLOR = Color(0x18181b)
    }

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

    fun <E> cycleButton(text: String, enums: List<E>, current: E, valueConsumer: (E) -> Unit): UIComponent where E : Enum<E>, E : TranslatableEnum {
        var currentIndex = enums.indexOf(current)
        val update = { "$text: ${I18n.get(enums[currentIndex].translationKey)}" }

        return button(update.invoke()) { str ->
            if (Screen.hasShiftDown()) {
                if (--currentIndex < 0)
                    currentIndex = enums.size - 1
            } else {
                if (++currentIndex >= enums.size)
                    currentIndex = 0
            }

            valueConsumer.invoke(enums[currentIndex])
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
        val outline = RoundedOutlinedBevelEffect(1f, OUTLINE_COLOR)
        val text = UIWrappedText(text, centered = true).constrain {
            this.x = CenterConstraint()
            this.y = CenterConstraint()
            this.width = 100.percent
            this.color = TEXT_COLOR.constraint
        }

        return UIRoundedRectangle(4f)
            .constrain {
                this.height = 18.pixels
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
                Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f))
                text.setText(onClick.invoke(this, text.getText()))
            }
    }

    fun expandableSection(text: String, builder: UIComponent.() -> Unit): ExpandableSection {
        return ExpandableSection(text, builder).constrain {
            this.x = CenterConstraint()
            this.y = SiblingConstraint() + 5.pixels
            this.width = 85.percentOfWindow
            this.height = ChildBasedSizeConstraint(4f)
        }
    }

    fun textInput(placeholder: String, value: String): UITextInput {
        return UITextInput(placeholder, inactiveSelectionBackgroundColor = BACKGROUND_COLOR)
            .constrain {
                this.color = TEXT_COLOR.constraint
            }
            .effect(RoundedOutlineEffect(1f, OUTLINE_COLOR))
            .apply {
                this.setText(value)
            }
    }

    fun slider(min: Int, max: Int, current: Int, updater: (Int) -> String): UISlider {
        val value = (current - min).toDouble() / (max - min).toDouble()
        return slider(0.0, 1.0, value) { updater.invoke((min.toDouble() + value * (max.toDouble() - min.toDouble())).toInt()) }
    }

    fun slider(min: Float, max: Float, current: Float, updater: (Float) -> String): UISlider {
        return slider(min.toDouble(), max.toDouble(), current.toDouble()) { updater.invoke(it.toFloat()) }
    }

    fun slider(min: Double, max: Double, current: Double, updater: (Double) -> String): UISlider {
        return UISlider(min, max, current, updater)
    }

    fun colorWithAlpha(rgb: Int, alpha: Int): Color {
        val rgba = (rgb shl 4) or alpha
        return Color(rgba, true)
    }

    fun ScrollComponent.withScrollbar(isHorizontal: Boolean = false): ScrollComponent {
        val scrollOutline = UIRoundedRectangle(4f).constrain {
            x = this@withScrollbar.constraints.x + this@withScrollbar.constraints.width
            y = this@withScrollbar.constraints.y
            width = 4.pixels
            height = this@withScrollbar.constraints.height
            color = Color.BLACK.constraint
        } childOf this.parent

        scrollOutline effect OutlineEffect(Color(0, 0, 0, 40), 1f, drawInsideChildren = true)

        val scrollbar = UIContainer().constrain {
            x = 1.pixels
            y = 0.pixels
            width = 2.pixels
            height = 30.percent
        } childOf scrollOutline

        val scrollBody = UIRoundedRectangle(4f).constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 100.percent
            color = Color.WHITE.constraint
        } childOf scrollbar

        scrollbar.animateBeforeHide {
            scrollOutline.hide()
        }

        scrollbar.animateAfterUnhide {
            scrollOutline.unhide()
        }

        this.setScrollBarComponent(scrollbar, hideWhenUseless = false, isHorizontal)
        return this
    }
}