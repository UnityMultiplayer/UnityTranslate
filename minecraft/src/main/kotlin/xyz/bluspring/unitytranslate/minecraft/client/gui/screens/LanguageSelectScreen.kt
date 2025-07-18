package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.button

class LanguageSelectScreen(val parent: Screen? = null, val onSelected: (Language?) -> Unit, val hasDefault: Boolean = false) : WindowScreen(ElementaVersion.V10) {
    val topText = UIText("Select Language").constrain {
        this.x = CenterConstraint()
        this.y = 12.pixels
    } childOf window

    val sections = ScrollComponent().constrain {
        this.x = CenterConstraint()
        this.y = 24.pixels
        this.width = 90.percentOfWindow
        this.height = 100.percentOfWindow - 24.pixels - 35.pixels
    }.apply {
        for (language in Language.entries) {

        }
    } childOf window

    val doneSection = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 100.percentOfWindow - 32.pixels
        this.width = 70.percentOfWindow
        this.height = 20.pixels
    }.apply {
        button("Close")
            .constrain {
                this.x = CenterConstraint() - 10.percent - 12.pixels
                this.width = 20.percent
            }
            .onMouseClick {
                onClose()
            } childOf this
    } childOf window

    constructor(parent: Screen? = null, onSelected: (Language) -> Unit) : this(parent, { onSelected.invoke(it!!) }, false)

    init {

    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }
}