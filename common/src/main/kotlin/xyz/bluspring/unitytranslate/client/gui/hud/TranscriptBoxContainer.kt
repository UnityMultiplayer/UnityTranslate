package xyz.bluspring.unitytranslate.client.gui.hud

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.alpha
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig

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
        graphics.pushMatrix()
        graphics.translate(this.x, this.y)

        // Background
        graphics.pushMatrix()

        when (val background = this.config.background) {
            is TranscriptBoxConfig.Background.Color -> {
                val (topLeft, topRight, bottomLeft, bottomRight) = ColorConfig.separateMatrix(background.color)
                graphics.fill(
                    -this.config.padding.left, -this.config.padding.top,
                    width + this.config.padding.right, height + this.config.padding.bottom,
                    topLeft, topRight,
                    bottomLeft, bottomRight
                )
            }

            is TranscriptBoxConfig.Background.Image -> {
                graphics.blit(
                    -this.config.padding.left, -this.config.padding.top,
                    width + this.config.padding.right, height + this.config.padding.bottom,
                    background.u, background.v,
                    background.uWidth, background.vHeight,
                    background.texture
                )
            }

            is TranscriptBoxConfig.Background.ImageWithOverlay -> {
                val image = background.image
                val (topLeft, topRight, bottomLeft, bottomRight) = ColorConfig.separateMatrix(background.color.color)
                graphics.blitWithColor(
                    -this.config.padding.left, -this.config.padding.top,
                    width + this.config.padding.right, height + this.config.padding.bottom,
                    image.u, image.v,
                    image.uWidth, image.vHeight,
                    image.texture,
                    topLeft, topRight,
                    bottomLeft, bottomRight
                )
            }
        }

        val outline = this.config.outline
        if (outline.thickness > 0) {
            val thickness = outline.thickness
            val (topLeft, topRight, bottomLeft, bottomRight) = ColorConfig.separateMatrix(outline.color)

            graphics.outline(-this.config.padding.left - thickness, -this.config.padding.top - thickness, width + this.config.padding.right + thickness, height + this.config.padding.bottom + thickness, thickness, topLeft, topRight, bottomLeft, bottomRight)
        }

        graphics.popMatrix()

        // Header
        graphics.text(FontReference.minecraft(font), headerText.visualOrderText, this.headerX, this.headerY, -1, this.config.header.hasShadow)

        val transcripts = synchronized(this.holder.transcripts) { this.holder.transcripts.toList() }
        var offset = 0f
        for (transcript in transcripts.reversed()) {
            val text = this.config.transcriptDisplay.text(transcript)

            for (sequence in font.split(text, this.width.toInt() - 4).reversed()) {
                val hasShadow = this.config.shadowColor.alpha() <= 10
                graphics.text(FontReference.minecraft(font), sequence, 0f, -offset, this.config.textColor, hasShadow) // TODO: shadow
                offset += 10
            }

            offset += 2
        }

        graphics.popMatrix()
    }

    fun updateConfig() {
        if (this.holder.language != this.config.language)
            this.holder = UnityTranslateApi.instance.getOrCreateTranscriptHolder(this.config.language)

        val screenWidth = (ClientPlatformProxy.instance.windowWidth / ClientPlatformProxy.instance.guiScale).toInt()
        val screenHeight = (ClientPlatformProxy.instance.windowHeight / ClientPlatformProxy.instance.guiScale).toInt()

        val pos = config.transforms.position.calculatePos(screenWidth, screenHeight)
        val dimensions = config.transforms.size.calculateDimensions(pos, screenWidth, screenHeight)

        this.x = dimensions.left
        this.y = dimensions.top
        this.width = dimensions.right - dimensions.left
        this.height = dimensions.bottom - dimensions.top

        this.headerText = this.config.header.text(this.holder.language)
        val headerLength = font.width(this.config.header.display.text(Component.empty()))
        val languageLength = font.width(this.config.header.langDecoration.decorate(this.config.header.langDisplay.text(this.holder.language)))

        this.headerX = this.config.header.alignX.align(this.width, headerLength, languageLength)
        this.headerY = this.config.header.alignY.align(this.height)
    }
}
