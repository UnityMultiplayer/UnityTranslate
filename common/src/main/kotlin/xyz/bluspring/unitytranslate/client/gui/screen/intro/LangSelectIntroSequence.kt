package xyz.bluspring.unitytranslate.client.gui.screen.intro

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.DropdownList
import xyz.bluspring.unitytranslate.client.gui.element.UILabel
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen

class LangSelectIntroSequence(parent: FirstStartupScreen) : IntroSequence(parent) {
    override fun init(width: Int, height: Int) {
        super.init(width, height)

        val languages: suspend () -> Collection<Language> = {
            UnityTranslateApiImpl.translators.values.flatMap { it.getSupportedLanguages() }.distinct()
        }

        val visualizer: (Language) -> Component= { language ->
            Component.translatable("unitytranslate.language.native_and_localized",
                Component.translatableWithFallback("unitytranslate.language.${language.serialized}.native", language.formatted),
                Component.translatableWithFallback("unitytranslate.language.${language.serialized}.localized", language.formatted)
            )
        }


        val elementWidth = 160f
        val elementHeight = 15f
        val font = ClientPlatformProxy.instance.defaultFont

        val xPos = 12f
        val yPos = height / 2f - (((elementHeight + 20f) * (UnityTranslateApiImpl.outputLanguages.size)) / 2f)

        this.addChild(UILabel(xPos, yPos - 6f, Component.translatable("config.unitytranslate.unitytranslate.languages.spoken").append(": "), font, alignX = UILabel.HorizontalAlign.LEFT, alignY = UILabel.VerticalAlign.CENTER))
        this.addChild(DropdownList(xPos, yPos, elementWidth, elementHeight, font, languages, visualizer,
            UnityTranslateApiImpl::currentSpokenLanguage))

        var index = 1
        for ((id, langHolder) in UnityTranslateApiImpl.outputLanguages) {
            val offset = (index++) * (elementHeight + 20f)

            this.addChild(UILabel(xPos, yPos + offset - 6f, Component.translatable("config.unitytranslate.unitytranslate.languages.$id").append(": "), font, alignX = UILabel.HorizontalAlign.LEFT, alignY = UILabel.VerticalAlign.CENTER))
            this.addChild(DropdownList(xPos, yPos + offset, elementWidth, elementHeight, font, languages, visualizer,
                langHolder::languageOrNull, DropdownList.Type.DEFAULTED))
        }
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float) {
    }
}
