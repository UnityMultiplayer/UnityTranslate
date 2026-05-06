package xyz.bluspring.unitytranslate.shared.update

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.bluspring.unitytranslate.shared.LaunchConfig
import java.net.URI
import kotlin.time.Duration.Companion.hours

object UpdateHelper {
    private var cachedLatestUpdateData: UpdateData? = null
    private var lastVersionCheck: Long = -1

    suspend fun getLatestVersion(): String? {
        return getLatestUpdateData()?.version
    }

    suspend fun getLatestUpdateData(config: LaunchConfig = LaunchConfig()): UpdateData? {
        if (System.currentTimeMillis() - lastVersionCheck <= 2.hours.inWholeMilliseconds) {
            return cachedLatestUpdateData
        }

        return try {
            val url = URI.create(config.updateUrl).toURL()
            val json = withContext(Dispatchers.IO) {
                url.openStream()
            }.use { JsonParser.parseReader(it.reader()) }

            ChannelBasedUpdateData.CODEC.decode(JsonOps.INSTANCE, json).orThrow.first.channels[config.channel]
        } catch (e: Throwable) {
            throw e
        } finally {
            lastVersionCheck = System.currentTimeMillis()
        }.apply {
            cachedLatestUpdateData = this
        }
    }

    suspend fun checkHasUpdate(version: String?): Boolean {
        if (version == null)
            return true // No version detected, well now we have to update!

        val latestVersion = this.getLatestVersion()
            ?: return false // We can't safely determine the latest version at the moment.

        return isVersionNewer(version, latestVersion)
    }

    fun isVersionNewer(current: String, latestVersion: String): Boolean {
        // Remove all special characters that delimit stuff (0.2.0-beta, 1.2.4+1.21.1)
        val actualLatestVersion = latestVersion.split("-")[0].split("+")[0]
        val currentVersion = current.split("-")[0].split("+")[0]

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