package xyz.bluspring.unitytranslate.client.gui.hud.ingame

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper.bytesToNearestLarge
import xyz.bluspring.unitytranslate.api.v2.download.DownloadInfo
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.config.ColorConfig
import xyz.bluspring.unitytranslate.client.gui.element.UIElement
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig
import java.nio.file.Path
import kotlin.io.path.name

class DownloadProgressHud : UIElement() {
    val currentDownloads: Map<Path, DownloadInfo>
        get() {
            return DownloadHelper.activeDownloads
        }

    override fun bounds(
        screenWidth: Int,
        screenHeight: Int
    ): ScreenRectangle {
        if (this.currentDownloads.isNotEmpty()) {
            val width = 200
            val height = 100

            return ScreenRectangle(screenWidth - width, screenHeight - height, width, height)
        }

        return ScreenRectangle.empty()
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        super.submit(graphics, partialTick, mouseX, mouseY)

        val downloads = this.currentDownloads.toMap()
        if (downloads.isNotEmpty()) {
            val bounds = this.bounds()

            graphics.enableScissor(bounds.left(), bounds.top(), bounds.width, bounds.height)
            graphics.fill(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), ColorConfig.separateMatrix(ThemeConfig.downloadBackground))
            graphics.outline(bounds.left().toFloat(), bounds.top().toFloat(), bounds.right().toFloat(), bounds.bottom().toFloat(), 1f, ColorConfig.separateMatrix(ThemeConfig.downloadOutline))

            val font = ClientPlatformProxy.instance.defaultFont
            val textColor = ThemeConfig.downloadText

            val x = bounds.left() + 2
            var y = bounds.top() + 2

            graphics.text(font, Component.literal("Downloading ${downloads.size} files..."), x.toFloat(), y.toFloat(), textColor, true)
            y += font.lineHeight

            val downloadBarBgMatrix = ColorConfig.separateMatrix(ThemeConfig.downloadProgressBackground)
            val downloadBarFillMatrix = ColorConfig.separateMatrix(ThemeConfig.downloadProgressFill)

            for ((path, info) in downloads) {
                graphics.text(font, Component.literal("Downloading ${path.name}"), x.toFloat(), y.toFloat(), textColor, false)
                graphics.fill(x.toFloat(), y.toFloat() + font.lineHeight, bounds.right().toFloat() - 2, y.toFloat() + font.lineHeight + 2, downloadBarBgMatrix)
                graphics.fill(x.toFloat(), y.toFloat() + font.lineHeight, x.toFloat() + ((bounds.width() - 4) * info.progress).toFloat(), y.toFloat() + font.lineHeight + 2, downloadBarFillMatrix)
                graphics.text(font, Component.literal("${info.downloadedBytes.bytesToNearestLarge()} / ${info.totalBytes.bytesToNearestLarge()}"), x.toFloat(), y.toFloat() + font.lineHeight + 5, textColor, false)

                y += font.lineHeight * 2 + 4
            }

            graphics.disableScissor()
        }
    }
}
