package xyz.bluspring.unitytranslate.gui.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.font.FontProvider
import gg.essential.elementa.utils.roundToRealPixels
import gg.essential.universal.ChatColor
import gg.essential.universal.UMatrixStack
import gg.essential.universal.standalone.nanovg.NvgContext
import gg.essential.universal.standalone.nanovg.NvgFont
import gg.essential.universal.standalone.nanovg.NvgFontFace
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui
import java.awt.Color

object CustomFontRenderer : FontProvider {
    const val SECTION_SIGN = '§'

    val LIGHT_FONT = loadFont("Light")
    val REGULAR_FONT = loadFont("Regular")
    val MEDIUM_FONT = loadFont("Medium")
    val SEMIBOLD_FONT = loadFont("SemiBold")
    val BOLD_FONT = loadFont("Bold")

    var currentFont: NvgFont = REGULAR_FONT

    fun loadFont(style: String): NvgFont {
        return NvgFont(NvgFontFace(NvgContext(),
            UnityTranslateGui::class.java.getResource("/fonts/TikTokSans-$style.ttf")!!.readBytes()),
            8f, 6f, 1f
        )
    }

    override fun getBaseLineHeight(): Float {
        return 6f
    }

    override fun getBelowLineHeight(): Float {
        return 1f
    }

    override fun getShadowHeight(): Float {
        return 1f
    }

    override fun getStringHeight(string: String, pointSize: Float): Float {
        return 8f
    }

    override fun getStringWidth(string: String, pointSize: Float): Float {
        var text = ""

        var isInSection = false
        for (char in string) {
            if (char == SECTION_SIGN) {
                isInSection = true
            } else if (isInSection) {
                isInSection = false
            } else {
                text += char
            }
        }

        return currentFont.getStringWidth(text, pointSize)
    }

    override var cachedValue: FontProvider = this
    override var constrainTo: UIComponent? = null
    override var recalculate: Boolean = false

    override fun visitImpl(
        visitor: ConstraintVisitor,
        type: ConstraintType
    ) {
    }

    override fun drawString(
        matrixStack: UMatrixStack,
        string: String,
        color: Color,
        x: Float,
        y: Float,
        originalPointSize: Float,
        scale: Float,
        shadow: Boolean,
        shadowColor: Color?
    ) {
        val scaledY = y.roundToRealPixels() / scale

        matrixStack.scale(scale, scale, 1f)

        if ((color.rgb shr 24 ) and 255 > 10) {
            val texts = mutableListOf<Triple<Int, Style, String>>()
            var currentText = ""
            var currentColor = color
            var currentStyle = Style.REGULAR

            var isInSection = false

            fun pushText() {
                texts.add(Triple(currentColor.rgb, currentStyle, currentText))
                currentText = ""
            }

            for (char in string) {
                if (char == SECTION_SIGN) {
                    isInSection = true
                    continue
                } else if (isInSection) {
                    isInSection = false
                    val chatColor = ChatColor.entries.firstOrNull { it.char == char } ?: continue

                    if (chatColor.color != null) {
                        pushText()
                        currentColor = chatColor.color!!
                    } else if (chatColor.isFormat) {
                        pushText()
                        if (chatColor == ChatColor.BOLD)
                            currentStyle = Style.BOLD
                    } else if (chatColor == ChatColor.RESET) {
                        pushText()
                        currentColor = color
                        currentStyle = Style.REGULAR
                    }

                    continue
                }

                currentText += char
            }

            pushText()

            var currentX = x
            for ((rgb, style, text) in texts) {
                val scaledX = currentX.roundToRealPixels() / scale

                currentFont = if (style == Style.REGULAR)
                    REGULAR_FONT
                else
                    MEDIUM_FONT

                currentFont.drawString(matrixStack, text, Color(rgb), scaledX, scaledY, originalPointSize, scale, shadow, shadowColor)

                currentX += currentFont.getStringWidth(text, originalPointSize)
                currentFont = REGULAR_FONT
            }
        }

        matrixStack.scale(1 / scale, 1 / scale, 1f)
    }

    enum class Style {
        REGULAR, BOLD
    }
}