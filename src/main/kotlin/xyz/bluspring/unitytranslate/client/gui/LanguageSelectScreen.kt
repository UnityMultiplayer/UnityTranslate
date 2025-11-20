package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.client.gui.screens.Screen
//? if >= 1.21.9 {
/*import net.minecraft.client.input.MouseButtonEvent
*///?}
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.client.UnityTranslateClient
import xyz.bluspring.unitytranslate.network.UTClientNetworking
import xyz.bluspring.unitytranslate.util.multiversion.*

class LanguageSelectScreen(val parent: Screen?, val type: LanguageSelectType) : Screen(Component.translatable("options.language")) {
    private lateinit var list: LanguageSelectionList

    override fun init() {
        super.init()

        list = LanguageSelectionList()
        this.addRenderableWidget(list)
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_DONE) {
                this.onDone()
            }
                .bounds(this.width / 2 - (Button.DEFAULT_WIDTH / 2), this.height - 38, Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT)
                .build()
        )
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parent)
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        //? if >= 1.20.4 {
        /*this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        *///?} else {
        this.renderBackground(guiGraphics)
        //?}
        super.render(guiGraphics, mouseX, mouseY, partialTick)

        guiGraphics.drawCenteredString(font, Component.translatable(
            when (type) {
                LanguageSelectType.TRANSCRIPT_BOX -> "unitytranslate.select_language"
                LanguageSelectType.SPOKEN -> "unitytranslate.set_spoken_language"
                LanguageSelectType.BALLOON -> "unitytranslate.set_balloon_language"
            }
        ), this.width / 2, 15, 16777215)

        UnityTranslateClient.renderCreditText(guiGraphics)
    }

    private fun onDone() {
        val language = list.selected?.language

        if (language == null) {
            onClose()
            return
        }

        if (type == LanguageSelectType.TRANSCRIPT_BOX) {
            if (list.selected?.shouldBeDeactivated == true) {
                onClose()
                return
            }

            Minecraft.getInstance().execute {
                UnityTranslate.config.client.transcriptBoxes.add(TranscriptBox(0, 0, 150, 170, 120, language))
                UnityTranslate.saveConfig()
                UnityTranslateClient.updateConfig()

                UTClientNetworking.updateLanguagesToServer()
            }
        } else {
            if (type == LanguageSelectType.SPOKEN) {
                UnityTranslate.config.client.language = language
                UnityTranslateClient.transcriber.changeLanguage(language)
            } else {
                if (list.selected!!.isBalloonDefault)
                    UnityTranslate.config.client.setBalloonLanguage(null)
                else
                    UnityTranslate.config.client.setBalloonLanguage(language)
            }

            UnityTranslate.saveConfig()
            UnityTranslateClient.updateConfig()

            UTClientNetworking.updateLanguagesToServer()
        }

        onClose()
    }

    private inner class LanguageSelectionList : ObjectSelectionList<LanguageSelectionList.Entry>(Minecraft.getInstance(),
        this@LanguageSelectScreen.width, this@LanguageSelectScreen.height
        //? if >= 1.20.4 {
        /*- 75
        *///?}
        , 32,
        //? if <= 1.20.1 {
        this@LanguageSelectScreen.height - 65 + 4,
        //?}
        18
    ) {
        init {
            if (type == LanguageSelectType.BALLOON) {
                val default = Entry(UnityTranslate.config.client.language, true)
                this.addEntry(default)

                if (UnityTranslate.config.client.isBalloonDefaultLanguage()) {
                    this.selected = default
                }
            }

            for (language in Language.entries.sortedBy { it.code }) {
                val entry = Entry(language)
                this.addEntry(entry)

                if (type == LanguageSelectType.SPOKEN && UnityTranslateClient.transcriber.language == language) {
                    this.selected = entry
                } else if (type == LanguageSelectType.BALLOON && UnityTranslate.config.client.balloonLanguage == language && !UnityTranslate.config.client.isBalloonDefaultLanguage()) {
                    this.selected = entry
                }
            }
        }

        inner class Entry(val language: Language, var isBalloonDefault: Boolean = false) : ObjectSelectionList.Entry<Entry>() {
            internal val shouldBeDeactivated = type == LanguageSelectType.TRANSCRIPT_BOX && UnityTranslate.config.client.transcriptBoxes.any { it.language == language }
            private var lastClickTime: Long = 0L

            //? if >= 1.21.9 {
            /*override fun renderContent(
                guiGraphics: GuiGraphics,
                mouseX: Int, mouseY: Int,
                isHovering: Boolean,
                partialTick: Float
            *///?} else {
            override fun render(
                guiGraphics: GuiGraphics,
                index: Int, top: Int, left: Int,
                width: Int, height: Int,
                mouseX: Int, mouseY: Int,
                hovering: Boolean, partialTick: Float
            //?}
            ) {
                //? if >= 1.21.9 {
                /*val top = this.y
                *///?}

                val color = if (shouldBeDeactivated) {
                    0x656565
                } else 0xFFFFFF

                if (isBalloonDefault)
                    guiGraphics.drawCenteredString(font, Component.translatable("unitytranslate.select_language.balloon_default", language.text), this@LanguageSelectScreen.width / 2, top + 1, color)
                else
                    guiGraphics.drawCenteredString(font, language.text, this@LanguageSelectScreen.width / 2, top + 1, color)

                if (type != LanguageSelectType.TRANSCRIPT_BOX) {
                    var x = this@LanguageSelectScreen.width / 2 + (font.width(language.text) / 2) + 4
                    for (type in language.supportedTranscribers.keys) {
                        if (!type.enabled)
                            continue

                        guiGraphics.blitTexture(UnityTranslate.id("textures/gui/transcriber/${type.name.lowercase()}.png"),
                            x, top - 1, 0f, 0f, 16, 16, 16, 16
                        )

                        if (mouseX >= x && mouseX <= x + 16 && mouseY >= top - 1 && mouseY <= top - 1 + 16) {
                            val lines = mutableListOf<FormattedCharSequence>()

                            lines.add(Component.translatable("unitytranslate.transcriber.type.${type.name.lowercase()}").visualOrderText)
                            lines.add(Component.empty().visualOrderText)
                            lines.addAll(font.split(Component.translatable("unitytranslate.transcriber.type.${type.name.lowercase()}.description"), (this@LanguageSelectScreen.width / 6).coerceAtLeast(150)))

                            guiGraphics.renderTooltip(font, lines, mouseX, mouseY)
                        }

                        x += 20
                    }
                } else if (shouldBeDeactivated) {
                    val textWidth = font.width(language.text)
                    val halfTextWidth = textWidth / 2
                    val centerX = this@LanguageSelectScreen.width / 2

                    if (mouseX >= centerX - halfTextWidth && mouseX <= centerX + halfTextWidth && mouseY >= top + 1 && mouseY <= top + 1 + font.lineHeight) {
                        guiGraphics.renderTooltip(font, listOf(
                            Component.translatable("unitytranslate.select_language.already_selected").visualOrderText
                        ), mouseX, mouseY)
                    }
                }
            }

            //? if >= 1.21.9 {
            /*override fun mouseClicked(event: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
                val button = event.button()
            *///?} else {
            override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            //?}
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
                return Component.translatable("narrator.select", this.language.text)
            }
        }
    }
}