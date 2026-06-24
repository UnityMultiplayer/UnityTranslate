package xyz.bluspring.unitytranslate.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsExtractorAccessor {
    @Accessor("guiRenderState")
    GuiRenderState unitytranslate$getGuiRenderState();

    @Accessor("scissorStack")
    GuiGraphicsExtractor.ScissorStack unityTranslate$getScissorStack();
}
