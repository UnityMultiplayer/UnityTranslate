package xyz.bluspring.unitytranslate.transcriber.browser

import net.minecraft.Util
import net.minecraft.Util.OS
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

enum class UTBrowserType(val defaultLocations: Map<OS, List<String>>) {
    CHROME(mapOf(
        OS.WINDOWS to listOf(
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe"
        ),
        OS.OSX to listOf(
            "/Applications/Chrome.app"
        ),
        OS.LINUX to listOf(
            "/usr/bin/google-chrome-stable",
            "/usr/bin/google-chrome"
        )
    )),
    EDGE(mapOf(
        OS.WINDOWS to listOf(
            "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
            "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
        )
    ));

    fun findExistingPath(): Path? {
        val os = Util.getPlatform()

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