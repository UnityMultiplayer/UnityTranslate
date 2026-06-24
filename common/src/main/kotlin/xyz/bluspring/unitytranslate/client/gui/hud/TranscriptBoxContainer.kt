package xyz.bluspring.unitytranslate.client.gui.hud

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.client.renderer.ui.UIGraphics

class TranscriptBoxContainer(var holder: TranscriptHolder, val config: TranscriptBoxConfig) {
    var x = 0f
    var y = 0f
    var width = 0f
    var height = 0f
    var font = Minecraft.getInstance().font

    private var headerText: Component = Component.empty()
    private var headerX: Float = 0f
    private var headerY: Float = 0f

    fun submit(graphics: UIGraphics, partialTick: Float) {
        graphics.poseStack.pushPose()
        graphics.poseStack.translate(this.x, this.y, 0f)

        // Background
        graphics.poseStack.pushPose()
        graphics.poseStack.translate(0f, 0f, 1f)

        val background = this.config.background
        if (background is TranscriptBoxConfig.Background.Color) {
            val color = background.color
            val topLeft = if (color is ColorConfig.Solid) color.color else if (color is ColorConfig.Gradient) color.colors[0] else -1
            val topRight = if (color is ColorConfig.Solid) color.color else if (color is ColorConfig.Gradient) color.colors[1] else -1
            val bottomLeft = if (color is ColorConfig.Solid) color.color else if (color is ColorConfig.Gradient) color.colors[2] else -1
            val bottomRight = if (color is ColorConfig.Solid) color.color else if (color is ColorConfig.Gradient) color.colors[3] else -1

            graphics.fill(-this.config.padding.left, -this.config.padding.top, width + this.config.padding.right, height + this.config.padding.bottom, topLeft, topRight, bottomLeft, bottomRight)
        }
        graphics.poseStack.popPose()

        // Header
        graphics.poseStack.pushPose()
        graphics.poseStack.translate(0f, 0f, 2f)

        graphics.drawString(font, headerText.visualOrderText, this.headerX, this.headerY, -1, this.config.header.hasShadow)
        graphics.poseStack.popPose()

        graphics.poseStack.popPose()
    }

    fun updateConfig() {
        if (this.holder.languageCode != this.config.languageCode)
            this.holder = UnityTranslateApi.instance.getOrCreateTranscriptHolder(this.config.languageCode)

        val screenWidth = (ClientPlatformProxy.instance.windowWidth / ClientPlatformProxy.instance.guiScale).toInt()
        val screenHeight = (ClientPlatformProxy.instance.windowHeight / ClientPlatformProxy.instance.guiScale).toInt()

        val pos = config.transforms.position.calculatePos(screenWidth, screenHeight)
        val dimensions = config.transforms.size.calculateDimensions(pos, screenWidth, screenHeight)

        this.x = dimensions.left
        this.y = dimensions.top
        this.width = dimensions.right - dimensions.left
        this.height = dimensions.bottom - dimensions.top

        this.headerText = this.config.header.text(this.holder.languageCode)
        val headerLength = font.width(this.config.header.display.text(Component.empty()))
        val languageLength = font.width(this.config.header.langDecoration.decorate(this.config.header.langDisplay.text(this.holder.languageCode)))

        this.headerX = this.config.header.alignX.align(this.width, headerLength, languageLength)
        this.headerY = this.config.header.alignY.align(this.height)
    }
}
