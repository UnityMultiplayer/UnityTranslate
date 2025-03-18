package xyz.bluspring.unitytranslate.minecraft.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.unitytranslate.minecraft.duck.ScrollableWidget;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin implements ScrollableWidget {
    @Shadow public int x;
    @Shadow public int y;

    @Shadow protected int width;
    @Shadow protected int height;
    @Unique private int initialX;
    @Unique private int initialY;
    //#if MC < 1.20.1
    @Unique private Component tooltip;

    @NotNull
    @Override
    public Component unityTranslate$getTooltip() {
        return this.tooltip;
    }

    @Override
    public void unityTranslate$setTooltip(@NotNull Component component) {
        this.tooltip = component;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void unityTranslate$renderTooltip(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance().screen != null && this.tooltip != null && mouseX >= this.x && mouseX <= (this.x + this.width) && mouseY >= this.y && mouseY <= (this.y + this.height)) {
            Minecraft.getInstance().screen.renderTooltip(poseStack, this.tooltip, mouseX, mouseY);
        }
    }
    //#endif

    @Inject(method = "<init>", at = @At("TAIL"))
    private void unityTranslate$setInitialPositions(int x, int y, int width, int height, Component message, CallbackInfo ci) {
        this.unityTranslate$updateInitialPosition();
    }


    @Override
    public int unityTranslate$getInitialX() {
        return this.initialX;
    }

    @Override
    public int unityTranslate$getInitialY() {
        return this.initialY;
    }

    @Override
    public void unityTranslate$updateInitialPosition() {
        this.initialX = this.x;
        this.initialY = this.y;
    }
}
