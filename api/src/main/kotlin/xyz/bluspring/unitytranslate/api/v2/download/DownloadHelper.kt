package xyz.bluspring.unitytranslate.api.v2.download

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.DigestOutputStream
import kotlin.io.path.*

/**
 * Utilities to allow for downloading files in parallel without worrying about having to manage it yourself.
 */
object DownloadHelper {
    @JvmField val MAX_DOWNLOAD_THREADS = Runtime.getRuntime().availableProcessors().coerceAtMost(8)
    private val context = Dispatchers.IO.limitedParallelism(MAX_DOWNLOAD_THREADS) + CoroutineName("UnityTranslate Download Helper")
    private val scope = CoroutineScope(context)
    private val logger: Logger = LoggerFactory.getLogger("UnityTranslate Download Helper")

    @JvmStatic
    fun <T : URLProvider> findSuitableUrl(urls: Collection<T>): T? {
        if (urls.isEmpty())
            throw IllegalArgumentException("URLs cannot be empty!")

        if (urls.size == 1)
            return urls.first()

        for (url in urls) {
            try {
                val connection = url.url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "HEAD"
                    if (connection.responseCode == 200)
                        return url
                } finally {
                    connection.disconnect()
                }
            } catch (_: Throwable) {}
        }

        return null
    }

    @JvmStatic
    fun findSuitableUrl(urls: Collection<URL>): URL? {
        if (urls.isEmpty())
            throw IllegalArgumentException("URLs cannot be empty!")

        if (urls.size == 1)
            return urls.first()

        for (url in urls) {
            try {
                val connection = url.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "HEAD"
                    if (connection.responseCode == 200)
                        return url
                } finally {
                    connection.disconnect()
                }
            } catch (_: Throwable) {}
        }

        return null
    }

    @JvmStatic @JvmOverloads
    fun queue(
        entry: DownloadableEntry,
        createTemp: Boolean = true,
        overwrite: Boolean = false,
        vararg options: OpenOption = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
    ): DownloadInfo {
        return queue(entry.uri.toURL(), entry.path, entry.expectedHash, createTemp, overwrite, options = options)
    }

    // This only works for the Kotlin code, so.
    fun queue(
        url: URL, path: Path,
        sha1: String? = null,
        createTemp: Boolean = true,
        overwrite: Boolean = false,
        vararg options: OpenOption = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
    ): DownloadInfo {
        return queue(url, path, hash = if (sha1 != null) DownloadHash.Sha1(sha1) else null, createTemp = createTemp, overwrite = overwrite, options = options)
    }

    @JvmStatic @JvmOverloads
    fun queue(
        url: URL, path: Path,
        hash: DownloadHash? = null,
        createTemp: Boolean = true,
        overwrite: Boolean = false,
        vararg options: OpenOption = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
    ): DownloadInfo {
        val info = DownloadInfo.Mutable(path, url)

        if (!overwrite && path.exists()) {
            logger.debug("Already downloaded ${path.absolutePathString()}, no need to download it again.")
            info.deferred = CompletableDeferred(Unit)
            return info
        } else {
            logger.debug("Queued {} for download.", url)
        }

        val tempPath = if (createTemp) path.resolveSibling("${path.name}.tmp") else path

        info.deferred = scope.async(context) {
            val startTime = System.currentTimeMillis()

            try {
                info.onStartDownload.invoker().run()
                info.hasStarted = true

                logger.debug("Started download of {} to {}, {}.",
                    url,
                    path.absolutePathString(),
                    if (createTemp) "a temp file will be created" else "a temp file will not be created"
                )
                path.createParentDirectories() // Just in case...

                val digest = hash?.createDigest()

                tempPath.outputStream(*options).use { fileStream ->
                    val actualStream = DigestOutputStream(fileStream, digest)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "HEAD"
                    info.totalBytes = connection.contentLengthLong

                    url.openStream().use { downloadStream ->
                        var b: Int
                        do {
                            b = downloadStream.read()

                            if (b == -1)
                                break

                            actualStream.write(b)
                            info.downloadedBytes++
                        } while (true)
                    }
                }

                // Time to validate the file and make sure it's correct.
                if (hash?.hash != null && digest != null) {
                    val actualHash = digest.digest().toHexString(HexFormat.Default)
                    if (actualHash != hash.hash) {
                        throw SecurityException("Invalid ${hash.type} hash for ${path.name}! (expected: ${hash.hash}, got: $actualHash)")
                    }
                }

                if (createTemp) {
                    val newStartTime = System.currentTimeMillis()
                    logger.debug("Download of {} to {} was completed in ${System.currentTimeMillis() - startTime} ms, making permanent file...", url, path.absolutePathString())
                    tempPath.moveTo(path, true)
                    logger.debug("{} has been made a permanent file. (took ${System.currentTimeMillis() - newStartTime} ms)", path.absolutePathString())
                } else {
                    logger.debug("Download of {} to {} was completed in ${System.currentTimeMillis() - startTime} ms.", url, path.absolutePathString())
                }
            } catch (e: Throwable) {
                logger.error("Failed to download $url into ${path.absolutePathString()}!", e)
                throw e
            }
        }.apply {
            invokeOnCompletion {
                info.onFinishDownload.invoker().run()
            }
        }

        return info
    }

    @JvmStatic
    fun Long.bytesToNearestLarge(): String {
        var value = this.toDouble()
        var type = "KiB"

        if (value <= 1024.0 * 1024) {
            value /= 1024.0
            type = "KiB"
        } else if (value <= 1024.0 * 1024 * 1024) {
            value /= 1024.0 * 1024.0
            type = "MiB"
        } else if (value <= 1024.0 * 1024 * 1024 * 1024) {
            value /= 1024.0 * 1024.0 * 1024.0
            type = "GiB"
        }

        return "%.2f $type".format(value)
    }
}
