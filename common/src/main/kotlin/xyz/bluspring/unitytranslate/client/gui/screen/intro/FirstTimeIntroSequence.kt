package xyz.bluspring.unitytranslate.client.gui.screen.intro

import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.withAlpha
import xyz.bluspring.unitytranslate.api.v2.util.CommonEasing
import xyz.bluspring.unitytranslate.client.gui.LogoTransitionOverlay
import xyz.bluspring.unitytranslate.client.gui.element.PlainUIButton
import xyz.bluspring.unitytranslate.client.gui.screen.FirstStartupScreen

class FirstTimeIntroSequence(parent: FirstStartupScreen) : IntroSequence(parent) {
    init {
        this.children.add(PlainUIButton(
            UnityTranslateApi.instance.defaultFont, Component.literal("unitytranslate.intro.first_time.next").withStyle(ChatFormatting.UNDERLINE), { width, _ -> width / 2f }, { _, height -> height / 2f + 80f }) {
                this.parent.next()
            }
        )
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int, transitionProgress: Float) {
        for (element in this.children) {
            if (element is PlainUIButton) {
                element.color = element.color.withAlpha(transitionProgress)
            }
        }

        val size = 80f

        graphics.pushMatrix()
        graphics.translate(graphics.width / 2f - (size / 2f), (graphics.height / 2f - (size / 2f)) - Mth.lerp(CommonEasing.SMOOTH.getValue(transitionProgress), 0f, 32f))
        graphics.blitWithColor(0f, 0f, size, size, 0f, 0f, 1f, 1f, LogoTransitionOverlay.logoTexture, if (this.isReversed)
            (-1).withAlpha(CommonEasing.SMOOTH.getValue(transitionProgress))
        else -1)
        graphics.popMatrix()

        val font = Minecraft.getInstance().font
        val splitText = font.split(Component.translatable("unitytranslate.intro.first_time"), 315)

        graphics.pushMatrix()
        graphics.translate(0f, Mth.lerp(1f - CommonEasing.SMOOTH.getValue(transitionProgress), 0f, 4f))
        for ((index, text) in splitText.withIndex()) {
            graphics.centeredText(UnityTranslateApi.instance.defaultFont, text,
                graphics.width / 2f, graphics.height / 2f + 40f + (index * font.lineHeight),
                0xFFFFFF.withAlpha(transitionProgress), true)
        }
        graphics.popMatrix()
    }
}
