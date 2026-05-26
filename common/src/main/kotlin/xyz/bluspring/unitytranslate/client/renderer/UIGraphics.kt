package xyz.bluspring.unitytranslate.client.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.LightCoordsUtil
import java.util.*

class UIGraphics {
    val poseStack = PoseStack()
    private val scissorState = Stack<ScreenRectangle>()

    fun enableScissor(x: Int, y: Int, width: Int, height: Int) {
        this.scissorState.push(ScreenRectangle(x, y, width, height))
    }

    fun disableScissor() {
        this.scissorState.pop()
    }

    fun drawString(font: Font, text: Component, x: Float, y: Float, color: Int, dropShadow: Boolean)
        = drawString(font, text.visualOrderText, x, y, color, dropShadow)

    fun drawString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean) {
        val pose = poseStack.last()
        val prepared = font.prepareText(text, x, y, color, dropShadow, true, 0)
        prepared.visit(object : Font.GlyphVisitor {
            override fun acceptEffect(effect: TextRenderable) {
                accept(effect)
            }

            override fun acceptGlyph(glyph: TextRenderable.Styled) {
                accept(glyph)
            }

            private fun accept(glyph: TextRenderable) {
                val consumer = BatchedGuiRenderer.getBuffer(glyph.guiPipeline(), textures = listOf(
                    BatchedGuiRenderer.Texture("Sampler0", glyph.textureView())
                ))
                glyph.render(pose.pose(), consumer, LightCoordsUtil.FULL_BRIGHT, false)
            }
        })
    }
}
