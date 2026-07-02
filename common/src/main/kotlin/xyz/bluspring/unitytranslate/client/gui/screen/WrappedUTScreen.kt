package xyz.bluspring.unitytranslate.client.gui.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.client.renderer.ui.MinecraftUIGraphics

class WrappedUTScreen(val actualScreen: UTScreen, private val parent: Screen? = null) : Screen(Component.empty()) {
    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        if (this.minecraft.level == null) {
            this.extractPanorama(graphics, a)
        }

        this.extractBlurredBackground(graphics)
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        this.actualScreen.setup(width, height)
    }

    override fun tick() {
        super.tick()
        actualScreen.tick()
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        actualScreen.submit(MinecraftUIGraphics(graphics), Minecraft.getInstance().deltaTracker.getGameTimeDeltaPartialTick(true), mouseX, mouseY)
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        return actualScreen.mouseClicked(event.x, event.y, event.button())
    }

    override fun mouseScrolled(x: Double, y: Double, scrollX: Double, scrollY: Double): Boolean {
        return actualScreen.mouseScrolled(x, y, scrollX, scrollY)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        return actualScreen.mouseReleased(event.x, event.y, event.button())
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        return actualScreen.charTyped(event.codepoint)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        return actualScreen.keyPressed(event.key, event.scancode, event.modifiers)
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        return actualScreen.keyReleased(event.key, event.scancode, event.modifiers)
    }
}
