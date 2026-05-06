package xyz.bluspring.unitytranslate.standalone.launch

import xyz.bluspring.unitytranslate.shared.OperatingSystem
import xyz.bluspring.unitytranslate.shared.OperatingSystem.Type

object LauncherConstants {
    const val MC_VERSION_MANIFEST = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    const val JAVA_DOWNLOAD_URL = "https://aka.ms/download-jdk/microsoft-jdk-"

    fun buildJava(javaVersion: String): String? {
        val os = when (OperatingSystem.type) {
            Type.WINDOWS -> "windows"
            Type.MAC -> "macos"
            Type.LINUX -> "linux"
            else -> return null
        }

        val arch = when (System.getProperty("os.arch").lowercase()) {
            "amd64", "x86_64" -> "x64"
            "aarch64", "arm64" -> "aarch64"
            else -> return null
        }

        val extension = when (OperatingSystem.type) {
            Type.WINDOWS -> "zip"
            else -> "tar.gz"
        }

        return "${JAVA_DOWNLOAD_URL}${javaVersion}-$os-$arch.$extension"
    }
}