package xyz.bluspring.unitytranslate.util.nativeaccess

import xyz.bluspring.unitytranslate.UnityTranslate

object LwjglLoader {
    private var isLoaded = false

    fun tryLoadLwjgl() {
        if (isLoaded)
            return

        try {
            Class.forName("org.lwjgl.Version")
            UnityTranslate.logger.info("LWJGL was detected, not loading custom-bundled LWJGL.")
        } catch (_: Exception) {
            UnityTranslate.logger.info("LWJGL was not detected, attempting to load custom-bundled LWJGL.")

            try {
                loadBundledLwjgl()
            } catch (e: Exception) {
                UnityTranslate.logger.error("Failed to load custom-bundled LWJGL!")
                e.printStackTrace()
            }
        }

        isLoaded = true
    }

    private fun loadBundledLwjgl() {
        val dir = "/lwjgl"

        /*val version = LwjglLoader::class.java.getResource("$dir/version.txt")?.readText(Charsets.UTF_8) ?: throw IllegalStateException("Missing LWJGL version data!")
        val fileName = "lwjgl-$version-natives-${when (Util.getPlatform()) {
            Util.OS.WINDOWS -> "windows"
            Util.OS.OSX -> "macos"
            Util.OS.LINUX -> "linux"
            else -> throw IllegalStateException("Unsupported platform ${Util.getPlatform()}!")
        }}.jar"
        val nativesJar = LwjglLoader::class.java.getResource("$dir/$fileName") ?: throw IllegalStateException("Missing LWJGL natives for platform ${Util.getPlatform()}!")

        if (!LocalLibreTranslateInstance.unityTranslateDir.exists())
            LocalLibreTranslateInstance.unityTranslateDir.mkdirs()

        val file = File(LocalLibreTranslateInstance.unityTranslateDir, fileName)

        if (!file.exists())
            file.createNewFile()

        file.writeBytes(nativesJar.readBytes())
        System.setProperty("org.lwjgl.librarypath", file.absolutePath)*/

        // TODO: figure out how to load LWJGL on Forge.
        // The module class loader makes it absurdly difficult to add custom JARs to the classpath,
        // meaning we likely need to make a custom mod loader or language provider *just* to get LWJGL to load ON THE SERVER SIDE ONLY.

        // by the way, if you're wondering how to make this idea work in Fabric, it's just this:
        // FabricLauncherBase.getLauncher().addToClassPath(path)
    }
}