package xyz.bluspring.unitytranslate.util.multiversion

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
//? if >= 1.21.8 {
/*import net.minecraft.client.renderer.RenderPipelines
*///?} else {
import net.minecraft.client.renderer.RenderType
//?}
import net.minecraft.resources./*? if < 1.21.11 {*/ResourceLocation/*?} else {*//*Identifier as ResourceLocation*//*?}*/
import net.minecraft.util.FormattedCharSequence

fun GuiGraphics.blitTexture(texture: ResourceLocation, x: Int, y: Int, u: Float, v: Float, uWidth: Int, vHeight: Int, textureWidth: Int, textureHeight: Int) {
    //? if >= 1.21.8 {
    /*this.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, uWidth, vHeight, textureWidth, textureHeight)
    *///?} else if >= 1.21.4 {
    /*this.blit(RenderType::guiTextured, texture, x, y, u, v, uWidth, vHeight, textureWidth, textureHeight)
    *///?} else {
    this.blit(texture, x, y, u, v, uWidth, vHeight, textureWidth, textureHeight)
    //?}
}

//? if >= 1.21.9 && < 1.21.11 {
/*fun GuiGraphics.renderOutline(x: Int, y: Int, width: Int, height: Int, color: Int) {
    this.submitOutline(x, y, width, height, color)
}
*///?}

//? if >= 1.21.8 {
/*fun GuiGraphics.renderTooltip(font: Font, components: List<FormattedCharSequence>, x: Int, y: Int) {
    this.setTooltipForNextFrame(font, components, x, y)
}
*///?}