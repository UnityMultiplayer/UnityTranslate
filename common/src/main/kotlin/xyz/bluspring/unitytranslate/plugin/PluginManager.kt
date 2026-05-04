package xyz.bluspring.unitytranslate.plugin

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.UnityTranslatePlugin
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import java.util.jar.JarFile
import kotlin.io.path.extension
import kotlin.io.path.walk

object PluginManager {
    val plugins: MutableMap<PluginMetadata, UnityTranslatePlugin> = mutableMapOf()
    private var isLoaded = false
    private val logger: Logger = LoggerFactory.getLogger("UnityTranslate Plugin Manager")

    private fun locatePluginJars(): Collection<Path> {
        return PlatformProxy.instance.pluginsDir.walk()
            .filter { it.extension == "jar" }
            .filter {
                try {
                    val jar = JarFile(it.toFile())
                    jar.getJarEntry("unitytranslate.plugin.json") != null
                } catch (_: Throwable) {
                    false
                }
            }
            .toList()
    }

    private fun createClassLoader(): ClassLoader {
        val jars = locatePluginJars()
        return URLClassLoader(jars.map { it.toUri().toURL() }.toTypedArray(), this::class.java.classLoader)
    }

    fun loadPlugins() {
        if (isLoaded)
            return

        isLoaded = true

        val classLoader = createClassLoader()
        val plugins = ServiceLoader.load(UnityTranslatePlugin::class.java, classLoader)
        for (plugin in plugins) {
            try {
                val metadata = PluginMetadata.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(plugin::class.java.getResource("/unitytranslate.plugin.json")!!.readText()))
                    .orThrow.first

                this.plugins[metadata] = plugin
            } catch (e: Throwable) {
                logger.error("Failed to load plugin metadata for class ${plugin::class.java.name}!", e)
            }
        }

        for ((metadata, plugin) in this.plugins.toMap()) {
            try {
                plugin.onLoadPlugin(UnityTranslateApi.instance, metadata)
                logger.info("Loaded plugin ${metadata.name} (${metadata.fullId}) v${metadata.version} by ${metadata.authors.joinToString(", ")}.")
            } catch (e: Throwable) {
                logger.error("Failed to load plugin ${metadata.name} (${metadata.fullId})!", e)
                this.plugins.remove(metadata)
            }
        }
    }

    fun getPluginMetadataById(id: String): PluginMetadata? {
        return this.plugins.keys.firstOrNull { it.fullId == id }
    }
}
