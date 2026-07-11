package xyz.bluspring.unitytranslate.client.gui.screen.intro

import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.withAlpha
import xyz.bluspring.unitytranslate.api.v2.util.CommonEasing
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.LogoTransitionOverlay
import xyz.bluspring.unitytranslate.client.gui.element.PlainUIButton
import xyz.bluspring.unitytranslate.client.gui.element.UILabel
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen

class DownloadIntroSequence(parent: FirstStartupScreen) : IntroSequence(parent) {
    private lateinit var downloadText: UILabel

    override fun init(width: Int, height: Int) {
        super.init(width, height)

        val font = ClientPlatformProxy.instance.defaultFont
        this.downloadText = this.addChild(UILabel(width / 2f, height / 2f, Component.translatable("unitytranslate.intro.download_required"), font, alignX = UILabel.HorizontalAlign.CENTER, maxWidth = width - 40))

        val yPos = height - ((height - this.children.maxOf { it.getBounds(width, height).bottom() } + (font.lineHeight * 2)) / 2f)
        val proceedText = Component.translatable("unitytranslate.intro.download_required.proceed")
        val backText = Component.translatable("unitytranslate.intro.download_required.back")

        this.addChild(PlainUIButton(font, proceedText, width / 2f, yPos) {
            this.parent.next()
        })

        this.addChild(PlainUIButton(font, backText, width / 2f, yPos + font.lineHeight + 2) {
            this.parent.back()
        })
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float) {
        val size = 64f

        graphics.pushMatrix()
        graphics.translate(graphics.width / 2f - (size / 2f), this.downloadText.bounds().top() / 2f - Mth.lerp(CommonEasing.SMOOTH.getValue(transitionProgress), 24f, 32f))
        graphics.blitWithColor(0f, 0f, size, size, 0f, 0f, 1f, 1f, LogoTransitionOverlay.logoTexture, (-1).withAlpha(CommonEasing.SMOOTH.getValue(transitionProgress)))
        graphics.popMatrix()
    }
}
