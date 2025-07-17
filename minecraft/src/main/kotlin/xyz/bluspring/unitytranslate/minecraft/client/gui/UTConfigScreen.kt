package xyz.bluspring.unitytranslate.minecraft.client.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.UIConstraints
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
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
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import xyz.bluspring.unitytranslate.minecraft.client.gui.elementa.RoundedOutlineEffect
import java.awt.Color

class UTConfigScreen(private val parent: Screen?) : WindowScreen(ElementaVersion.V10) {
    val buttonColor = Color(0x282828)

    val topText = UIText("UnityTranslate").constrain {
        this.x = CenterConstraint()
        this.y = 12.pixels
    } childOf window

    val sections = ScrollComponent().constrain {
        this.x = CenterConstraint()
        this.y = 24.pixels
        this.width = 90.percentOfWindow
        this.height = 90.percentOfWindow
    }.apply {
        fun UIConstraints.defaultButtonConstraints() {
            this.x = CramSiblingConstraint(5f)
            this.width = 40.percentOfWindow
            this.y = CramSiblingConstraint(5f)
        }

        // Client Section
        expandableSection(I18n.get("gui.unitytranslate.config.client")) {
            val config = UnityTranslateMCClient.clientConfig

            toggleButton("Mod Status", config.enabled) { config.enabled = it }
                .constrain { defaultButtonConstraints() } childOf this

            toggleButton("Mute Transcript when Voice Chat Muted?", config.muteTranscriptWhenVoiceChatMuted) { config.muteTranscriptWhenVoiceChatMuted = it }
                .constrain { defaultButtonConstraints() } childOf this
        } childOf this

        // Common Section
        expandableSection(I18n.get("gui.unitytranslate.config.common")) {
            val config = UnityTranslate.instance.config.common

            toggleButton("Should use CUDA?", config.shouldUseCuda) { config.shouldUseCuda = it }
                .constrain { defaultButtonConstraints() } childOf this
        } childOf this

        // Server Section
        expandableSection(I18n.get("gui.unitytranslate.config.server")) {

        } childOf this
    } childOf window

    val doneSection = UIContainer().constrain {
        this.x = CenterConstraint()
        this.y = 100.percentOfWindow - 32.pixels
        this.width = 70.percentOfWindow
        this.height = 20.pixels
    }.apply {
        button("Save & Exit")
            .constrain {
                this.x = CenterConstraint() - 10.percent - 12.pixels
                this.width = 20.percent
            } childOf this

        button("Reset All to Default")
            .constrain {
                this.x = CenterConstraint() + 10.percent + 12.pixels
                this.width = 20.percent
            } childOf this
    } childOf window

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }

    private fun toggleButton(text: String, current: Boolean, valueConsumer: (Boolean) -> Unit): UIComponent {
        var current = current
        var color = if (current)
            ChatFormatting.GREEN
        else
            ChatFormatting.RED
        val button = button { "$text: $color${if (current) "Enabled" else "Disabled"}" }

        return button
            .onMouseClick {
                current = !current
                color = if (current)
                    ChatFormatting.GREEN
                else
                    ChatFormatting.RED
                valueConsumer.invoke(current)
            }
    }

    private fun button(text: String): UIComponent {
        return button { text }
    }

    private fun button(textProvider: () -> String): UIComponent {
        val outline = RoundedOutlineEffect(1f, Color.BLACK)

        return UIRoundedRectangle(4f)
            .constrain {
                this.height = 20.pixels
                this.color = buttonColor.constraint
            }
            .effect(outline)
            .apply {
                UIWrappedText(textProvider.invoke(), centered = true).constrain {
                    this.x = CenterConstraint()
                    this.y = CenterConstraint()
                    this.width = 100.percent
                } childOf this
            }
            .onMouseEnter {
                outline.color = Color.WHITE
            }
            .onMouseLeave {
                outline.color = Color.BLACK
            }
    }

    private fun expandableSection(text: String, builder: UIComponent.() -> Unit): UIComponent {
        return UIContainer().constrain {
            this.x = CenterConstraint()
            this.y = SiblingConstraint() + 5.pixels
            this.width = 85.percentOfWindow
            this.height = ChildBasedSizeConstraint(4f)
        }.apply {
            val container = UIContainer().constrain {
                this.x = CenterConstraint()
                this.y = SiblingConstraint() + 5.pixels

                this.width = 80.percentOfWindow + 10.pixels
                this.height = ChildBasedSizeConstraint(4f) + 8.pixels
            }
                .apply {
                    builder.invoke(this)
                }

            // Expand button
            button(text)
                .constrain {
                    this.x = CenterConstraint()
                    this.y = 4.pixels
                    this.width = 85.percentOfWindow
                }
                .apply {
                    val upArrow = "▲"
                    val downArrow = "▼"

                    val arrowText = UIText(downArrow).constrain {
                        this.x = 100.percent - 16.pixels
                        this.y = CenterConstraint()
                    } childOf this

                    onMouseClick {
                        if (arrowText.getText() == upArrow) {
                            arrowText.setText(downArrow)
                            container.hide()
                        } else {
                            arrowText.setText(upArrow)
                            container.unhide()
                        }
                    }
                } childOf this

            // Expanded container just below
            container childOf this
            container.hide(true)
        }
    }
}