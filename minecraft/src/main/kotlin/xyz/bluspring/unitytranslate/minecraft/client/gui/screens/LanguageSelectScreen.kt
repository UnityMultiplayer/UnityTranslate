package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.universal.UMatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.library.util.DownloadHelper
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.button
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.withScrollbar
import kotlin.time.Duration.Companion.seconds

class LanguageSelectScreen(val parent: Screen? = null, val onSelected: (Language?) -> Unit, val hasDefault: Boolean = false, val checkRequiresDownload: (Language) -> Boolean = { false }) : WindowScreen(ElementaVersion.V10) {
    val background = UIBlock(ElementaUIHelpers.BACKGROUND_COLOR).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 100.percentOfWindow
        this.height = 100.percentOfWindow
    } childOf window

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
        val clientConfig = UnityTranslateMCClient.clientConfig

        if (hasDefault) {
            val container = UIContainer()
                .constrain {
                    this.x = 0.pixels
                    this.y = SiblingConstraint() + 4.pixels
                    this.height = 20.pixels
                    this.width = 100.percent
                } childOf this

            button("Use Default")
                .constrain {
                    this.x = CenterConstraint()
                    this.y = SiblingConstraint() + 4.pixels
                    this.width = 250.pixels
                } childOf container
        }

        for (language in Language.entries.sortedBy { it.name }) {
            val requiresDownload = checkRequiresDownload.invoke(language)
            val container = UIContainer()
                .constrain {
                    this.x = 0.pixels
                    this.y = SiblingConstraint() + 4.pixels
                    this.height = 20.pixels
                    this.width = 100.percent
                } childOf this

            button(I18n.get(language.translationKey))
                .constrain {
                    this.x = CenterConstraint()
                    this.y = SiblingConstraint() + 4.pixels
                    this.width = 250.pixels
                }.apply {
                    isDisabled = requiresDownload
                } childOf container
        }
    }
        .withScrollbar() childOf window

    val doneSection = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 100.percentOfWindow - 32.pixels
        this.width = 70.percentOfWindow
        this.height = 20.pixels
    }.apply {
        button("Close")
            .constrain {
                this.x = CenterConstraint()
                this.width = 20.percent
            }
            .onMouseClick {
                onClose()
            } childOf this
    } childOf window

    constructor(parent: Screen? = null, onSelected: (Language) -> Unit) : this(parent, { onSelected.invoke(it!!) }, false)

    init {
        if (!UnityTranslateMCClient.clientConfig.backgroundEnabled) {
            background.hide(true)
        }
    }

    private var timeSinceLastPoll = 0L
    private val progressHandlers = mutableListOf<() -> Unit>()

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)

        if (System.currentTimeMillis() - timeSinceLastPoll >= 1000) {
            for (handler in progressHandlers) {
                handler.invoke()
            }

            timeSinceLastPoll = System.currentTimeMillis()
        }
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }
}