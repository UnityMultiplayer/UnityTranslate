package xyz.bluspring.unitytranslate.gui.transcriber.browser

import org.lwjgl.system.Platform
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

enum class UTBrowserType(val defaultLocations: Map<Platform, List<String>>) {
    CHROME(mapOf(
        Platform.WINDOWS to listOf(
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        ),
        Platform.MACOSX to listOf(
            "/Applications/Chrome.app"
        ),
        Platform.LINUX to listOf(
            "/usr/bin/google-chrome-stable",
            "/usr/bin/google-chrome"
        )
    )),
    EDGE(mapOf(
        Platform.WINDOWS to listOf(
            "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
            "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
        )
    ));

    fun findExistingPath(): Path? {
        val os = Platform.get()

        if (!defaultLocations.contains(os))
            return null

        for (location in defaultLocations[os]!!) {
            val path = Path(location)

            if (path.exists())
                return path
        }

        return null
    }
}