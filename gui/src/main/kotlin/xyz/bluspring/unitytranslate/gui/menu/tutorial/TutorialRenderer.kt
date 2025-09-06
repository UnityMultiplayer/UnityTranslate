package xyz.bluspring.unitytranslate.gui.menu.tutorial

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UMath
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import xyz.bluspring.unitytranslate.gui.elementa.CustomFontRenderer
import xyz.bluspring.unitytranslate.gui.elementa.ElementaUIHelpers
import xyz.bluspring.unitytranslate.gui.elementa.elements.UIButton
import java.awt.Color

class TutorialRenderer {
    var isOpen = false

    val window = Window(ElementaVersion.V10)

    val background = UIBlock(Color(0, 0, 0, 64)).constrain {
        x = 0.pixels
        y = 0.pixels
        width = 100.percentOfWindow
        height = 100.percentOfWindow
    } childOf window

    val firstTimeInfo = UIRoundedRectangle(12f).apply {
        UIWrappedText("Welcome!\n\nThis seems to be your first time using UnityTranslate, would you like a guide for how to use it?", centered = true, shadow = false)
            .constrain {
                x = CenterConstraint()
                y = 8.pixels

                width = 90.percent
                fontProvider = CustomFontRenderer
            } childOf this

        /*UIContainer().apply {
            UIButton("GUI Scale: 1")
                .onClick {
                    UMinecraft.guiScale = (UMinecraft.guiScale % 3) + 1
                    this.text = "GUI Scale: ${UMinecraft.guiScale}"
                }
                .constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(4f)

                    width = 100.pixels
                } childOf this
        }
            .constrain {
                y = SiblingConstraint(12f)
                width = 100.percent
                height = ChildBasedSizeConstraint(4f)
            } childOf this*/

        UIContainer().apply {
            UIButton("Take me there!")
                .constrain {
                    x = SiblingConstraint(8f)
                    width = 100.pixels
                } childOf this

            UIButton("Nah, I'm good")
                .onClick {
                    isOpen = false
                }
                .constrain {
                    x = SiblingConstraint(8f)
                    width = 100.pixels
                } childOf this
        }
            .constrain {
                x = CenterConstraint() - 100.pixels
                y = SiblingConstraint(12f)
            } childOf this

        // padding
        UIContainer().constrain {
            height = 12.pixels
        } childOf this
    }
        .constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            width = 75.percentOfWindow
            height = ChildBasedSizeConstraint(12f)

            color = ElementaUIHelpers.BACKGROUND_COLOR.constraint
        } childOf window

    fun render(matrixStack: UMatrixStack) {
        if (!isOpen)
            return

        window.draw(matrixStack)
    }

    fun mouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isOpen)
            return false

        window.mouseClick(mouseX, mouseY, button)

        return window.children.any { it.isPointInside(mouseX.toFloat(), mouseY.toFloat()) }
    }
}