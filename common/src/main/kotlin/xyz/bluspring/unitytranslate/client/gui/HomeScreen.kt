package xyz.bluspring.unitytranslate.client.gui

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import java.awt.Color

class HomeScreen : UIContainer() {
    init {
        constrain {
            width = 100.percent
            height = 100.percent
        }

        UIText("Welcome to UnityTranslate").constrain {
            color = ConstantColorConstraint(Color.WHITE)
        } childOf this
    }
}
