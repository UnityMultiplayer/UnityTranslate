package xyz.bluspring.unitytranslate.minecraft.client.gui.screens

import com.mojang.blaze3d.vertex.PoseStack
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.events.UIClickEvent
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMouse
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.TranscriptBox
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ColorSelector
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.button
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.cycleButton
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.slider
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.ElementaUIHelpers.withScrollbar
import java.awt.Color
import kotlin.math.floor

class EditTranscriptBoxesScreen(val parent: Screen?) : WindowScreen(ElementaVersion.V10) {
    val background = UIBlock(ElementaUIHelpers.BACKGROUND_COLOR).constrain {
        this.x = 0.pixels
        this.y = 0.pixels
        this.width = 100.percentOfWindow
        this.height = 100.percentOfWindow
    } childOf window

    val doneSection = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 100.percentOfWindow - 32.pixels
        this.width = 70.percentOfWindow
        this.height = 20.pixels
    }.apply {
        button("Save")
            .constrain {
                this.x = CenterConstraint()
                this.width = 20.percent
            }
            .onMouseClick {
                onClose()
            } childOf this

        button("+")
            .constrain {
                this.x = CenterConstraint() + 10.percent + 20.pixels
                this.width = 18.pixels
            }
            .onMouseClick {
                Minecraft.getInstance().setScreen(LanguageSelectScreen(this@EditTranscriptBoxesScreen) {
                    UnityTranslateMCClient.clientConfig.transcriptBoxes.add(UnityTranslateClientConfig.TranscriptBoxConfig(it))
                    UnityTranslateMCClient.instance.updateConfig()
                })
            } childOf this
    } childOf window

    private val dropdownOpener: UIComponent.(UIClickEvent) -> Unit = { event ->
        if (this is TranscriptBox)
            createSettingDropdown(this, event.absoluteX, event.absoluteY)
    }

    var currentDropdown: UIComponent? = null

    init {
        if (!UnityTranslateMCClient.clientConfig.backgroundEnabled) {
            background.hide(true)
        }

        // Automatically close the dropdown if we click outside of the region.
        window.onMouseClick { event ->
            if (currentDropdown != null && !currentDropdown!!.isPointInside(event.absoluteX, event.absoluteY)) {
                currentDropdown!!.hide()
                window.removeChild(currentDropdown!!)
                currentDropdown = null
            }
        }
    }

    override fun initScreen(width: Int, height: Int) {
        super.initScreen(width, height)

        for (box in UnityTranslateMCClient.transcriptRenderer.renderedBoxes) {
            box.onMouseClick(dropdownOpener)
        }
    }

    fun createSettingDropdown(box: TranscriptBox, mouseX: Float, mouseY: Float) {
        val dropdownWidth = 100
        val dropdownHeight = 175
        val screenWidth = window.getWidth()
        val screenHeight = window.getHeight()

        val dropdown = UIContainer().constrain {
            this.x = if (mouseX + dropdownWidth >= screenWidth)
                ((mouseX - (screenWidth - dropdownWidth)) / screenWidth).percent
            else
                (mouseX / screenWidth).percent

            this.y = if (mouseY + dropdownHeight >= screenHeight)
                ((mouseY - (screenHeight - dropdownHeight)) / screenHeight).percent
            else
                (mouseY / screenHeight).percent

            this.width = dropdownWidth.pixels
            this.height = dropdownHeight.pixels
        } childOf window

        val scrollContainer = ScrollComponent(innerPadding = 1f).constrain {
            this.x = 0.pixels
            this.y = 0.pixels
            this.width = 100.percent
            this.height = 100.percent
        } childOf dropdown

        scrollContainer.withScrollbar()

        fun <T : UIComponent> T.defaultConstraints(): T {
            return this.constrain {
                this.x = CenterConstraint()
                this.y = SiblingConstraint() + 4.pixels
                this.width = 100.percent
                this.height = 18.pixels
            }
        }

        // and now it's time to actually add the buttons
        slider(10, 300, box.config.textScale) {
            box.config.textScale = it
            box.update()
            "Text Scale: $it%"
        }.defaultConstraints() childOf scrollContainer

        cycleButton("Header Type", UnityTranslateClientConfig.HeaderType.entries, box.config.headerType) {
            box.config.headerType = it
            box.update()
        }.defaultConstraints() childOf scrollContainer

        ColorSelector("Box Color", Color(box.config.color)) {
            box.config.color = it.rgb
            box.update()
        }

        slider(0, 255, box.config.opacity) {
            box.config.opacity = it
            box.update()
            "Box Opacity: ${"%.2f".format((it / 255f) * 100f)}%"
        }.defaultConstraints() childOf scrollContainer

        slider(0f, 32f, box.config.radius) {
            box.config.radius = it
            box.update()
            "Box Roundness: ${"%.2f".format(it)}px"
        }.defaultConstraints() childOf scrollContainer

        slider(0f, 32f, box.config.outlineThickness) {
            box.config.outlineThickness = it
            box.update()
            "Outline Thickness: ${"%.2f".format(it)}px"
        }.defaultConstraints() childOf scrollContainer

        ColorSelector("Outline Color", Color(box.config.outlineColor)) {
            box.config.outlineColor = it.rgb
            box.update()
        }

        slider(0, 255, box.config.outlineOpacity) {
            box.config.outlineOpacity = it
            box.update()
            "Outline Opacity: ${"%.2f".format((it / 255f) * 100f)}%"
        }.defaultConstraints() childOf scrollContainer

        button("${ChatFormatting.RED}Remove")
            .onMouseClick {
                UnityTranslateMCClient.clientConfig.transcriptBoxes.remove(box.config)
                UnityTranslateMCClient.instance.updateConfig()

                Minecraft.getInstance().setScreen(this@EditTranscriptBoxesScreen)
            }.defaultConstraints() childOf scrollContainer

        this.currentDropdown = dropdown
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        UnityTranslateMCClient.transcriptRenderer.render(matrixStack, partialTicks)
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        super.onMouseClicked(mouseX, mouseY, mouseButton)

        val (adjustedMouseX, adjustedMouseY) =
            if ((mouseX == floor(mouseX) && mouseY == floor(mouseY))) {
                val x = UMouse.Scaled.x
                val y = UMouse.Scaled.y

                mouseX + (x - floor(x)) to mouseY + (y - floor(y))
            } else {
                mouseX to mouseY
            }

        UnityTranslateMCClient.transcriptRenderer.window.mouseClick(adjustedMouseX, adjustedMouseY, mouseButton)
    }

    override fun onMouseReleased(mouseX: Double, mouseY: Double, state: Int) {
        super.onMouseReleased(mouseX, mouseY, state)
        UnityTranslateMCClient.transcriptRenderer.window.mouseRelease()
    }

    override fun onScreenClose() {
        UnityTranslateMCClient.instance.saveConfig()
        UnityTranslateMCClient.instance.updateConfig()

        for (box in UnityTranslateMCClient.transcriptRenderer.renderedBoxes) {
            box.mouseClickListeners.remove(dropdownOpener)
        }
    }

    override fun onClose() {
        super.onClose()
        Minecraft.getInstance().setScreen(parent)
    }
}