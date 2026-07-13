package xyz.bluspring.unitytranslate.client.gui.hud

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.navigation.ScreenDirection
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptHolder
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.alpha
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper.multiplyAlpha
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig
import xyz.bluspring.unitytranslate.client.gui.element.FocusableUIElement
import xyz.bluspring.unitytranslate.client.gui.element.UIElement
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig
import java.util.*
import kotlin.math.floor

class TranscriptBoxContainer(var holder: TranscriptHolder, val config: TranscriptBoxConfig) : UIElement(), FocusableUIElement {
    override var isFocused: Boolean = false
    var isInEditMode = false
    var isEditorManaged = false
    val movingDirections: EnumSet<ScreenDirection> = EnumSet.noneOf(ScreenDirection::class.java)

    var x = 0f
    var y = 0f
    var width = 0f
    var height = 0f
    var font = Minecraft.getInstance().font

    private var headerText: Component = Component.empty()
    private var headerX: Float = 0f
    private var headerY: Float = 0f

    private var startMouseX = 0
    private var startMouseY = 0

    override fun bounds(
        screenWidth: Int,
        screenHeight: Int
    ): ScreenRectangle {
        return ScreenRectangle(
            (this.x - this.config.padding.left - this.config.outline.thickness).toInt(),
            (this.y - this.config.padding.top - this.config.outline.thickness).toInt(),
            (this.width + this.config.padding.left + this.config.padding.right + (this.config.outline.thickness * 2)).toInt(),
            (this.height + this.config.padding.top + this.config.padding.bottom + (this.config.outline.thickness * 2)).toInt()
        )
    }

    override fun tick() {
        val transcripts = this.holder.transcripts.toList()
        var wasModified = false
        val transcriptsToRemove by lazy { mutableSetOf<TranscriptData>() }
        val ttl = (this.config.msToLive + this.config.msToFadeOut)

        for (transcript in transcripts) {
            if (System.currentTimeMillis() - transcript.timeUpdated >= ttl) {
                transcriptsToRemove.add(transcript)
                wasModified = true
            }
        }

        if (wasModified) {
            synchronized(this.holder.transcripts) {
                this.holder.transcripts.removeAll(transcriptsToRemove)
            }
        }
    }

    private val Long.ticks: Int
        get() {
            return floor(this / 50.0).toInt()
        }

    private val Int.ticks: Int
        get() {
            return floor(this / 50.0).toInt()
        }

    private val TranscriptData.ticks: Int
        get() {
            return floor((System.currentTimeMillis() - this.timeUpdated) / 50.0).toInt()
        }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
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

        graphics.enableScissor(0, font.lineHeight + 2, this.width.toInt(), this.height.toInt() - 7)
        graphics.pushMatrix()
        graphics.translate(0f, this.height - 8f)
        val transcripts = synchronized(this.holder.transcripts) { this.holder.transcripts.toList() }
            .sortedBy { it.timeUpdated }
        var offset = 0f
        val time = System.currentTimeMillis()
        val fadeTicks = this.config.msToFadeOut.ticks

        for (transcript in transcripts.reversed()) {
            val text = this.config.transcriptDisplay.text(transcript)
            val timeLived = time - transcript.timeUpdated

            val fadeMultiplier = if (timeLived >= this.config.msToLive)
                1f - Mth.clamp((timeLived.ticks - this.config.msToLive.ticks - partialTick) / fadeTicks.toFloat(), 0f, 1f)
            else 1f

            for (sequence in font.split(text, this.width.toInt() - 4).reversed()) {
                val hasShadow = this.config.shadowColor.alpha() <= 10
                graphics.text(FontReference.minecraft(font), sequence, 0f, -offset, this.config.textColor.multiplyAlpha(fadeMultiplier), hasShadow) // TODO: shadow
                offset += 10
            }

            offset += 2
        }
        graphics.popMatrix()
        graphics.disableScissor()

        graphics.popMatrix()

        super.submit(graphics, partialTick, mouseX, mouseY)

        if (this.isInEditMode) {
            val bounds = this.bounds()
            if (!this.isEditorManaged) {
                val newFocus = bounds.containsPoint(mouseX, mouseY)
                if (!this.isFocused && newFocus) {
                    this.startEditing(mouseX, mouseY)
                }

                this.isFocused = newFocus
            }

            if (this.isFocused) {
                val matrix = ColorConfig.separateMatrix(
                    if (this.movingDirections.isNotEmpty())
                        ThemeConfig.transcriptBoxOutlineMoving
                    else
                        ThemeConfig.transcriptBoxOutlineFocused
                )

                if (this.movingDirections.isNotEmpty()) {
                    graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f,
                        this.colorOrNone(matrix.topLeft, ScreenDirection.UP, ScreenDirection.LEFT), this.colorOrNone(matrix.topRight, ScreenDirection.UP, ScreenDirection.RIGHT),
                        this.colorOrNone(matrix.bottomLeft, ScreenDirection.DOWN, ScreenDirection.LEFT), this.colorOrNone(matrix.bottomRight, ScreenDirection.DOWN, ScreenDirection.RIGHT)
                    )
                } else {
                    graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, matrix)
                }
            }
        }
    }

    private fun colorOrNone(color: Int, vararg directions: ScreenDirection): Int {
        for (direction in directions) {
            if (this.movingDirections.contains(direction))
                return color
        }

        return 0
    }

    fun startEditing(mouseX: Int, mouseY: Int) {
        this.isInEditMode = true
        this.startMouseX = mouseX
        this.startMouseY = mouseY
    }

    private fun inRange(pos: Double, target: Int): Boolean {
        val range = 3.0
        return pos >= target - range && pos <= target + range
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.isInEditMode && button == 0 && this.isFocused) {
            val bounds = this.bounds()
            if (inRange(mouseX, bounds.left()))
                this.movingDirections.add(ScreenDirection.LEFT)

            if (inRange(mouseY, bounds.top()))
                this.movingDirections.add(ScreenDirection.UP)

            if (inRange(mouseY, bounds.bottom()))
                this.movingDirections.add(ScreenDirection.DOWN)

            if (inRange(mouseX, bounds.right()))
                this.movingDirections.add(ScreenDirection.RIGHT)

            if (this.movingDirections.isEmpty()) {
                this.movingDirections.addAll(ScreenDirection.entries)
            }

            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.isInEditMode && button == 0 && this.movingDirections.isNotEmpty()) {
            this.movingDirections.clear()
            return true
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    fun updateConfig() {
        if (this.holder.language != this.config.language)
            this.holder = UnityTranslateApi.instance.getOrCreateTranscriptHolder(this.config.language)

        val screenWidth = (ClientPlatformProxy.instance.windowWidth / ClientPlatformProxy.instance.guiScale).toInt()
        val screenHeight = (ClientPlatformProxy.instance.windowHeight / ClientPlatformProxy.instance.guiScale).toInt()

        val pos = config.transforms.position.calculatePos(screenWidth, screenHeight)
        val dimensions = config.transforms.size.calculateDimensions(pos, screenWidth, screenHeight)

        this.x = dimensions.left().toFloat()
        this.y = dimensions.top().toFloat()
        this.width = dimensions.width.toFloat()
        this.height = dimensions.height.toFloat()

        this.headerText = this.config.header.text(this.holder.language)
        val headerLength = font.width(this.config.header.display.text(Component.empty()))
        val languageLength = font.width(this.config.header.langDecoration.decorate(this.config.header.langDisplay.text(this.holder.language, this.config.header.langStyle)))

        this.headerX = this.config.header.alignX.align(this.width, headerLength, languageLength)
        this.headerY = this.config.header.alignY.align(this.height)
    }
}
