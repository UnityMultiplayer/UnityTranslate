package xyz.bluspring.unitytranslate.client.gui

import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.CommonEasing
import xyz.bluspring.unitytranslate.client.renderer.ui.UIGraphics
import xyz.bluspring.unitytranslate.client.renderer.ui.texture.MinecraftTextureReference
import kotlin.math.max

object LogoTransitionOverlay {
    const val TRANSITION_TIME = 1_500 // milliseconds
    private val logoTexture = MinecraftTextureReference(UnityTranslate.id("textures/gui/icon_transparent.png"))

    var startTime = 0L

    fun submit(graphics: UIGraphics, partialTick: Float) {
        if (this.startTime == 0L)
            return

        val minSize = 128f
        val maxSize = max(graphics.width, graphics.height).toFloat() + 512f
        val delta = ((System.currentTimeMillis() - this.startTime) / TRANSITION_TIME.toFloat()).coerceAtMost(1f)
        val size = Mth.clamp(Mth.lerp(CommonEasing.EASE_IN_CUBIC.getValue(delta), maxSize, minSize), minSize, maxSize)

        graphics.pushMatrix()
        graphics.translate(graphics.width / 2f - (size / 2f), graphics.height / 2f - (size / 2f))
        graphics.blitWithColor(0f, 0f, size, size, 0f, 0f, 1f, 1f, this.logoTexture, ARGBHelper.colorFromFloat(delta, 1f, 1f, 1f))
        graphics.popMatrix()
    }
}
