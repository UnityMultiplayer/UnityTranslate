package xyz.bluspring.unitytranslate.client.renderer.ui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiRenderState
import net.minecraft.util.FormattedCharSequence
import org.joml.Matrix3x2f
import xyz.bluspring.unitytranslate.api.v2.client.gui.TextureReference
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.MinecraftFontReference
import xyz.bluspring.unitytranslate.client.renderer.ui.minecraft.ColoredBlitRenderState
import xyz.bluspring.unitytranslate.client.renderer.ui.minecraft.GradientedFillRenderState
import xyz.bluspring.unitytranslate.client.renderer.ui.texture.AbstractTextureReference
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

    override fun text(font: FontReference, text: FormattedCharSequence, x: Float, y: Float, color: Int, dropShadow: Boolean) {
        graphics.text((font as MinecraftFontReference).font, text, x.toInt(), y.toInt(), color, dropShadow)
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

    override fun blitWithColor(
        x1: Float, y1: Float, x2: Float, y2: Float,
        u0: Float, v0: Float, u1: Float, v1: Float,
        texture: TextureReference,
        colorTopLeft: Int, colorTopRight: Int, colorBottomLeft: Int, colorBottomRight: Int
    ) {
        if (texture !is AbstractTextureReference)
            throw IllegalStateException("You are not supposed to extend TextureReference! Currently using ${texture::class.java.name}")

        graphics.guiRenderState.addGuiElement(ColoredBlitRenderState(RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(texture.textureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)),
            Matrix3x2f(graphics.pose()),
            graphics.scissor,
            x1, y1, x2, y2,

            colorTopLeft, colorTopRight,
            colorBottomLeft, colorBottomRight,

            ((u0 * texture.width) + (texture.u0 * texture.imageWidth)) / texture.imageWidth, ((v0 * texture.height) + (texture.v0 * texture.imageHeight)) / texture.imageHeight,
            ((u1 * texture.width) + (texture.u0 * texture.imageWidth)) / texture.imageWidth, ((v1 * texture.height) + (texture.v0 * texture.imageHeight)) / texture.imageHeight
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
