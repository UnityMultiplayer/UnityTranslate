package xyz.bluspring.unitytranslate.client.renderer.ui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix3x2f
import xyz.bluspring.unitytranslate.client.renderer.ui.minecraft.GradientedFillRenderState
import xyz.bluspring.unitytranslate.mixin.accessor.GuiGraphicsExtractorAccessor

class MinecraftUIGraphics(private val graphics: GuiGraphicsExtractor) : UIGraphics {
    private val GuiGraphicsExtractor.guiRenderState: GuiRenderState
        get() = (this as GuiGraphicsExtractorAccessor).`unitytranslate$getGuiRenderState`()

    private val GuiGraphicsExtractor.scissor: ScreenRectangle?
        get() = (this as GuiGraphicsExtractorAccessor).`unityTranslate$getScissorStack`().peek()

    override val width: Int
        get() = graphics.guiWidth()
    override val height: Int
        get() = graphics.guiHeight()

    override fun enableScissor(x: Int, y: Int, width: Int, height: Int) {
        graphics.enableScissor(x, y, x + width, y + height)
    }

    override fun disableScissor() {
        graphics.disableScissor()
    }

    override fun drawString(font: Font, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean) {
        graphics.text(font, text, x.toInt(), y.toInt(), color, dropShadow)
    }

    override fun fill(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        colorTopLeft: Int,
        colorTopRight: Int,
        colorBottomLeft: Int,
        colorBottomRight: Int
    ) {
        graphics.guiRenderState.addGuiElement(GradientedFillRenderState(RenderPipelines.GUI, TextureSetup.noTexture(),
            Matrix3x2f(graphics.pose()),
            graphics.scissor,
            x1, y1, x2, y2,
            colorTopLeft, colorTopRight, colorBottomLeft, colorBottomRight
        ))
    }

    override fun pushMatrix() {
        this.graphics.pose().pushMatrix()
    }

    override fun translate(x: Float, y: Float) {
        this.graphics.pose().translate(x, y)
    }

    override fun scale(x: Float, y: Float) {
        this.graphics.pose().scale(x, y)
    }

    override fun popMatrix() {
        this.graphics.pose().popMatrix()
    }
}
