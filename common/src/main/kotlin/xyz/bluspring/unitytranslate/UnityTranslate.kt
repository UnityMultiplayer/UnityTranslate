package xyz.bluspring.unitytranslate

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.plugin.PluginManager
import java.net.URI
import kotlin.time.Duration.Companion.hours

object UnityTranslate {
    const val MOD_ID = "unitytranslate"
    val logger: Logger = LoggerFactory.getLogger("UnityTranslate")

    fun init() {


        PluginManager.loadPlugins()
    }

    private var cachedLatestVersion: String? = null
    private var lastVersionCheck: Long = -1

    suspend fun getLatestVersion(): String? {
        if (System.currentTimeMillis() - lastVersionCheck <= 2.hours.inWholeMilliseconds) {
            return cachedLatestVersion
        }

        return try {
            val url = URI.create(Constants.UPDATE_URL).toURL()
            val json = withContext(Dispatchers.IO) {
                url.openStream()
            }.use { JsonParser.parseReader(it.reader()) }

            json.asJsonObject.get("tag_name").asString
        } catch (e: Throwable) {
            logger.error("Failed to get update info for UnityTranslate", e)
            null
        }.apply {
            cachedLatestVersion = this
            lastVersionCheck = System.currentTimeMillis()
        }
    }

    suspend fun checkHasUpdate(): Boolean {
        val latestVersion = this.getLatestVersion()
            ?: return false // We can't safely determine the latest version at the moment.

        // Remove all special characters that delimit stuff (0.2.0-beta, 1.2.4+1.21.1)
        val actualLatestVersion = latestVersion.split("-")[0].split("+")[0]
        val currentVersion = PlatformProxy.instance.version.split("-")[0].split("+")[0]

        val splitLatestVersion = actualLatestVersion.split(".")
        val splitCurrentVersion = currentVersion.split(".")

        for ((index, part) in splitLatestVersion.withIndex()) {
            if (part.toInt() > (splitCurrentVersion.getOrNull(index)?.toIntOrNull() ?: 0)) {
                return true
            }
        }

        return false
    }
}
