package xyz.bluspring.unitytranslate.client.gui.screen

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.withAlpha
import xyz.bluspring.unitytranslate.api.v2.util.CommonEasing
import xyz.bluspring.unitytranslate.client.gui.LogoTransitionOverlay

class FirstStartupScreen : UTScreen() {
    private val startTime = System.currentTimeMillis()
    val transitionTime = 1_200 // ms

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val delta = ((System.currentTimeMillis() - this.startTime) / transitionTime.toFloat()).coerceAtMost(1f)
        val size = 80f

        graphics.fill(0f, 0f, graphics.width.toFloat(), graphics.height.toFloat(),
            LogoTransitionOverlay.TOP_GRADIENT.withAlpha(1f), LogoTransitionOverlay.BOTTOM_GRADIENT.withAlpha(1f))

        graphics.pushMatrix()
        graphics.translate(graphics.width / 2f - (size / 2f), (graphics.height / 2f - (size / 2f)) - Mth.lerp(CommonEasing.SMOOTH.getValue(delta), 0f, 32f))
        graphics.blit(0f, 0f, size, size, 0f, 0f, 1f, 1f, LogoTransitionOverlay.logoTexture)
        graphics.popMatrix()

        val font = Minecraft.getInstance().font
        val splitText = font.split(Component.literal("Looks like this is your first time using UnityTranslate, let's help you get set up."), 315)

        graphics.pushMatrix()
        graphics.translate(0f, Mth.lerp(1f - CommonEasing.SMOOTH.getValue(delta), 0f, 4f))
        for ((index, text) in splitText.withIndex()) {
            graphics.centeredText(UnityTranslateApi.instance.defaultFont, text,
                graphics.width / 2f, graphics.height / 2f + 40f + (index * font.lineHeight),
                0xFFFFFF.withAlpha(delta), true)
        }
        graphics.popMatrix()
    }
}
