package xyz.bluspring.unitytranslate.gui.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.ChatColor
import gg.essential.universal.UI18n
import gg.essential.universal.UKeyboard
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui
import xyz.bluspring.unitytranslate.gui.elementa.effects.RoundedOutlineEffect
import xyz.bluspring.unitytranslate.gui.elementa.elements.ExpandableSection
import xyz.bluspring.unitytranslate.gui.elementa.elements.UIButton
import xyz.bluspring.unitytranslate.gui.elementa.elements.UISlider
import java.awt.Color
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

object ElementaUIHelpers {
    var DISABLED_BUTTON_COLOR = Color(0xFFFFFF)
    var BUTTON_COLOR = Color(0xFFFFFF)
    var BUTTON_HOVER_COLOR = Color(0xFFFFFF)
    var TEXT_COLOR = Color(0xFFFFFF)
    var BACKGROUND_COLOR = Color(0xFFFFFF)

    val OUTLINE_COLOR = Color.BLACK
    val OUTLINE_HOVER_COLOR = Color.WHITE

    init {
        setup()
    }

    fun setup() {
        if (UnityTranslateGui.clientConfig.isDarkMode)
            setupDarkMode()
        else
            setupLightMode()
    }

    fun setupLightMode() {
        BUTTON_COLOR = Color(0x707070)
        BUTTON_HOVER_COLOR = Color(0x9A9A9A)
        DISABLED_BUTTON_COLOR = Color(0x282828)
        TEXT_COLOR = Color.WHITE
        BACKGROUND_COLOR = Color(0xBABABA) // banana
    }

    fun setupDarkMode() {
        BUTTON_COLOR = Color(0x232323)
        BUTTON_HOVER_COLOR = Color(0x707070)
        DISABLED_BUTTON_COLOR = Color(0x181818)
        TEXT_COLOR = Color.WHITE
        BACKGROUND_COLOR = Color(0x18181b)
    }

    fun toggleButton(text: String, current: Boolean, valueConsumer: (Boolean) -> Unit): UIComponent {
        var current = current
        var color = if (current)
            ChatColor.GREEN
        else
            ChatColor.RED
        val update = { "$text: $color${if (current) "Enabled" else "Disabled"}" }

        return button(update.invoke()) { str ->
            current = !current
            color = if (current)
                ChatColor.GREEN
            else
                ChatColor.RED
            valueConsumer.invoke(current)
            update.invoke()
        }
    }

    fun <E> cycleButton(text: String, enums: List<E>, current: E, valueConsumer: (E) -> Unit): UIButton where E : Enum<E>, E : TranslatableEnum {
        var currentIndex = enums.indexOf(current)
        val update = { "$text: ${UI18n.i18n(enums[currentIndex].translationKey)}" }

        return button(update.invoke()) { str ->
            if (UKeyboard.isShiftKeyDown()) {
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

    fun button(text: String, onClick: UIComponent.() -> Unit): UIButton {
        return button(text) { str ->
            onClick.invoke(this)
            str
        }
    }

    fun button(text: String, onClick: UIComponent.(String) -> String = { it }): UIButton {
        return UIButton(text, onClick)
    }

    fun fileChooser(text: String, currentFile: Path?, onSelected: (Path?) -> String): UIButton {
        return button(text) { str ->
            val fileChooser = JFileChooser(currentFile?.parent?.toFile())
            fileChooser.addChoosableFileFilter(FileNameExtensionFilter("Executable files", "exe"))
            fileChooser.isMultiSelectionEnabled = false
            fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

            val returnValue = fileChooser.showOpenDialog(null)

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                onSelected.invoke(file.toPath())
            } else {
                onSelected.invoke(currentFile)
            }
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