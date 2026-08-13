package xyz.bluspring.unitytranslate.client.gui.hud

import net.minecraft.ChatFormatting
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.withAlpha
import xyz.bluspring.unitytranslate.util.PlatformConversion.withStyle
import java.awt.Color
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class QuickLanguageRadialSelector {
    private fun Int.wrap(max: Int): Int = ((this % max) + max) % max

    fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Double, mouseY: Double) {
        val centerX = graphics.width / 2.0
        val centerY = graphics.height / 2.0
        val diffX = mouseX - centerX
        val diffY = mouseY - centerY

        val font = UnityTranslateApi.instance.client.defaultFont

        val languages = listOf("English", "Español", "Portugués", "Français", "Svenska", "Bahasa Melayu", "Deutsch", "Nederlands")
        val totalLanguages = languages.size

        if (totalLanguages <= 0) {
            graphics.centeredText(
                font, TextComponent.literal("You do not currently have any quick languages assigned!")
                    .withStyle(ChatFormatting.RED), centerX.toFloat(), centerY.toFloat() - 4f, -1, true)
            return
        }

        // 1 = 0.0
        // 2 = 0.0
        // 3 = 90.0
        // 4 = 0.0
        // 5 = 18.0
        // 6 = 0.0
        // 7 = 13.0
        // 8 = 0.0
        // 9 = 10.0
        // 10 = 0.0
        val offsetAngle = 90.0
        val divisions = 360.0 / totalLanguages
        val angle = Math.toDegrees(Mth.atan2(diffY, diffX)) + 180.0
        var index = floor((angle / divisions) - (offsetAngle / divisions)).toInt() % totalLanguages
        if (index < 0)
            index += totalLanguages

        val debug = false
        val start = 50f
        val end = 100f

        graphics.pushMatrix()
        graphics.translate(centerX.toFloat(), centerY.toFloat())

        for (i in 0 until totalLanguages) {
            val langAngleStart = divisions * i + 180.0 + offsetAngle
            val langAngleMid = divisions * i + 180.0 + offsetAngle + (divisions / 2.0)
            val langAngleEnd = divisions * (i + 1) + 180.0 + offsetAngle
            val startX = cos(Math.toRadians(langAngleStart)).toFloat()
            val startY = sin(Math.toRadians(langAngleStart)).toFloat()
            val midX = cos(Math.toRadians(langAngleMid)).toFloat()
            val midY = sin(Math.toRadians(langAngleMid)).toFloat()
            val endX = cos(Math.toRadians(langAngleEnd)).toFloat()
            val endY = sin(Math.toRadians(langAngleEnd)).toFloat()

            val (alpha, distance) = if (i == index)
                0.85f to 0f
            else if (i < index)
                (0.65f - (0.05f * (index - i))).coerceAtLeast(0f) to (-2f * (index - i))
            else
                (0.65f - (0.05f * (i - index))).coerceAtLeast(0f) to (-2f * (i - index))
            val colorOuter = ARGBHelper.colorFromFloat(alpha, alpha, alpha, alpha)
            val colorInner = ARGBHelper.colorFromFloat(alpha * 0.8f, alpha * 0.8f, alpha * 0.8f, alpha * 0.8f)

            val end = end + distance
            val mid = end - ((end - start) / 2f) - 4f

            graphics.meshFill(startX * start, startY * start, startX * end, startY * end, endX * start, endY * start, endX * end, endY * end, colorInner, colorOuter, colorInner, colorOuter)
            val splitText = font.split(TextComponent.literal(languages[i]), 60)
            for ((n, value) in splitText.reversed().withIndex()) {
                graphics.centeredText(font, value, midX * mid, midY * mid - ((splitText.size / 2f) + (n * font.lineHeight)), (-1).withAlpha(alpha), true)
            }
        }

        if (debug) {
            for (i in 0 until totalLanguages) {
                val langAngleStart = divisions * i + 180.0 + offsetAngle
                val startX = cos(Math.toRadians(langAngleStart)).toFloat()
                val startY = sin(Math.toRadians(langAngleStart)).toFloat()

                val radius = 2
                val a = ((i + 1) / totalLanguages.toFloat())
                val color = Color.getHSBColor(a, 1f, 1f).rgb
                graphics.fill(startX * end - radius, startY * end - radius, startX * end + radius, startY * end + radius, color)
            }
        }

        graphics.popMatrix()

        val splitText = font.split(TextComponent.literal("Select spoken language"), 100)
        for ((i, text) in splitText.withIndex()) {
            graphics.centeredText(font, text, centerX.toFloat(), centerY.toFloat() - 6f - (splitText.size / 2f) + (i * font.lineHeight), -1, true)
        }

        if (debug) {
            graphics.centeredText(font, TextComponent.literal("$index"), centerX.toFloat(), centerY.toFloat(), -1, true)

            graphics.centeredText(font, TextComponent.literal("a: ${"%.2f deg / %.2f deg".format(angle, angle + offsetAngle)}"), centerX.toFloat(), centerY.toFloat() + 12, -1, true)
            graphics.centeredText(font, TextComponent.literal("d: ${"%.2f".format(divisions)} deg"), centerX.toFloat(), centerY.toFloat() + 24, -1, true)
            graphics.centeredText(font, TextComponent.literal("o: ${"%.2f".format(offsetAngle)} deg"), centerX.toFloat(), centerY.toFloat() + 36, -1, true)
        }
    }
}
