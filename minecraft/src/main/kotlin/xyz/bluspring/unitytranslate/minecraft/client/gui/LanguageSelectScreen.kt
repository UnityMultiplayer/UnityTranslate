package xyz.bluspring.unitytranslate.minecraft.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy.text
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient

class LanguageSelectScreen(val parent: Screen?, val isAddingBox: Boolean) : Screen(MinecraftProxy.translatable("options.language")) {
    private lateinit var list: LanguageSelectionList

    override fun init() {
        super.init()

        list = LanguageSelectionList()
        this.addRenderableWidget(list)
        this.addRenderableWidget(
            Button(this.width / 2 - (UTConfigScreen.BUTTON_WIDTH / 2), this.height - 38, UTConfigScreen.BUTTON_WIDTH, UTConfigScreen.BUTTON_HEIGHT,
                CommonComponents.GUI_DONE
            ) {
                this.onDone()
            }
        )
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(poseStack)
        super.render(poseStack, mouseX, mouseY, partialTick)

        drawCenteredString(poseStack, font, MinecraftProxy.translatable(
            if (isAddingBox)
                "unitytranslate.select_language"
            else
                "unitytranslate.set_spoken_language"
        ), (this.width / 2), 15, 16777215)

        UnityTranslateMCClient.renderCreditText(poseStack)
    }

    private fun onDone() {
        val language = list.selected?.language

        if (language == null) {
            onClose()
            return
        }

        if (isAddingBox) {
            if (list.selected?.shouldBeDeactivated == true) {
                onClose()
                return
            }

            Minecraft.getInstance().execute {
                UnityTranslateMCClient.clientConfig.transcriptBoxes.add(UnityTranslateClientConfig.TranscriptBoxConfig(language))
                UnityTranslateMCClient.instance.saveConfig()
                UnityTranslateMCClient.instance.updateConfig()
            }
        } else {
            UnityTranslateMCClient.clientConfig.spokenLanguage = language
            UnityTranslateMCClient.transcriber.changeLanguage(language)
            UnityTranslateMCClient.instance.saveConfig()
            UnityTranslateMCClient.instance.updateConfig()
        }

        onClose()
    }

    private inner class LanguageSelectionList : ObjectSelectionList<LanguageSelectionList.Entry>(Minecraft.getInstance(), this@LanguageSelectScreen.width, this@LanguageSelectScreen.height, 32, this@LanguageSelectScreen.height - 65 + 4, 18) {
        init {
            for (language in Language.entries.sortedBy { it.name }) {
                val entry = Entry(language)
                this.addEntry(entry)

                if (!isAddingBox && UnityTranslateMCClient.transcriber.language == language) {
                    this.selected = entry
                }
            }
        }

        inner class Entry(val language: Language) : ObjectSelectionList.Entry<Entry>() {
            internal val shouldBeDeactivated = isAddingBox && UnityTranslateMCClient.clientConfig.transcriptBoxes.any { it.language == language }
            private var lastClickTime: Long = 0L

            override fun render(
                poseStack: PoseStack,
                index: Int, top: Int, left: Int,
                width: Int, height: Int,
                mouseX: Int, mouseY: Int,
                hovering: Boolean, partialTick: Float
            ) {
                val color = if (shouldBeDeactivated) {
                    0x656565
                } else 0xFFFFFF

                drawCenteredString(poseStack, font, language.text, this@LanguageSelectScreen.width / 2, top + 1, color)

                if (!isAddingBox) {
                    var x = this@LanguageSelectScreen.width / 2 + (font.width(language.text) / 2) + 4
                    for (type in language.supportedTranscribers.keys) {
                        if (!type.enabled)
                            continue

                        RenderSystem.setShaderTexture(0, MinecraftProxy.id("textures/gui/transcriber/${type.name.lowercase()}.png"))
                        blit(poseStack,
                            x, top - 1, 0f, 0f, 16, 16, 16, 16
                        )

                        if (mouseX >= x && mouseX <= x + 16 && mouseY >= top - 1 && mouseY <= top - 1 + 16) {
                            val lines = mutableListOf<FormattedCharSequence>()

                            lines.add(MinecraftProxy.translatable("unitytranslate.transcriber.type.${type.name.lowercase()}").visualOrderText)
                            lines.add(MinecraftProxy.literal("").visualOrderText)
                            lines.addAll(font.split(MinecraftProxy.translatable("unitytranslate.transcriber.type.${type.name.lowercase()}.description"), (this@LanguageSelectScreen.width / 6).coerceAtLeast(150)))

                            renderTooltip(poseStack, lines, mouseX, mouseY)
                        }

                        x += 20
                    }
                } else if (shouldBeDeactivated) {
                    val textWidth = font.width(language.text)
                    val halfTextWidth = textWidth / 2
                    val centerX = this@LanguageSelectScreen.width / 2

                    if (mouseX >= centerX - halfTextWidth && mouseX <= centerX + halfTextWidth && mouseY >= top + 1 && mouseY <= top + 1 + font.lineHeight) {
                        renderTooltip(poseStack, listOf(
                            MinecraftProxy.translatable("unitytranslate.select_language.already_selected").visualOrderText
                        ), mouseX, mouseY)
                    }
                }
            }

            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
                if (shouldBeDeactivated)
                    return false

                if (button == 0) {
                    this@LanguageSelectionList.selected = this
                    if (Util.getMillis() - this.lastClickTime < 250L) {
                        this@LanguageSelectScreen.onDone()
                    }

                    this.lastClickTime = Util.getMillis()
                    return true
                } else {
                    this.lastClickTime = Util.getMillis()
                    return false
                }
            }

            override fun getNarration(): Component {
                return MinecraftProxy.translatable("narrator.select", this.language.text)
            }
        }
    }
}