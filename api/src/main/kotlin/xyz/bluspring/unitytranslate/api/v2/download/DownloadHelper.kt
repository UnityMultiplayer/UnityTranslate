package xyz.bluspring.unitytranslate.api.v2.download

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import kotlin.io.path.*

/**
 * Utilities to allow for downloading files in parallel without worrying about having to manage it yourself.
 */
object DownloadHelper {
    @JvmField val MAX_DOWNLOAD_THREADS = Runtime.getRuntime().availableProcessors().coerceAtMost(4)
    private val context = Dispatchers.IO.limitedParallelism(MAX_DOWNLOAD_THREADS) + CoroutineName("UnityTranslate Download Helper")
    private val logger: Logger = LoggerFactory.getLogger("UnityTranslate Download Helper")

    @JvmStatic
    fun queueJava(url: URL, path: Path, vararg options: OpenOption = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)): DownloadInfo {
        return queueJava(url, path, sha1 = null, options = options)
    }

    @JvmStatic
    fun queueJava(url: URL, path: Path,
                  sha1: String? = null,
                  createTemp: Boolean = true,
                  overwrite: Boolean = false,
                  vararg options: OpenOption = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)): DownloadInfo {
        return runBlocking {
            queue(url, path, *options, sha1 = sha1, createTemp = createTemp, overwrite = overwrite)
        }
    }

    suspend fun queue(
        url: URL, path: Path,
        vararg options: OpenOption = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
        sha1: String? = null,
        createTemp: Boolean = true,
        overwrite: Boolean = false,
        parentDeferred: CompletableDeferred<Unit>? = null,
    ): DownloadInfo {
        val info = DownloadInfo.Mutable(parentDeferred)

        if (!overwrite && path.exists()) {
            logger.debug("Already downloaded ${path.absolutePathString()}, no need to download it again.")
            info.deferred.complete(Unit)
            return info
        }

        val tempPath = if (createTemp) path.resolveSibling("${path.name}.tmp") else path

        withContext(context) {
            launch {
                val startTime = System.currentTimeMillis()

                try {
                    logger.debug("Started download of {} to {}, {}.",
                        url,
                        path.absolutePathString(),
                        if (createTemp) "a temp file will be created" else "a temp file will not be created"
                    )
                    path.createParentDirectories() // Just in case...

                    val digest = if (sha1 != null) MessageDigest.getInstance("SHA-1") else null

                    tempPath.outputStream(*options).use { fileStream ->
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "HEAD"
                        info.totalBytes = connection.contentLengthLong

                        url.openStream().use { downloadStream ->
                            var b: Int
                            do {
                                b = downloadStream.read()
                                fileStream.write(b)
                                digest?.update(b.toByte())
                                info.downloadedBytes++
                            } while (b != -1)
                        }
                    }

                    // Time to validate the file and make sure it's correct.
                    if (sha1 != null && digest != null) {
                        val actualHash = digest.digest().toHexString(HexFormat.Default)
                        if (actualHash != sha1) {
                            throw SecurityException("Invalid SHA-1 hash for ${path.name}! (expected: $sha1, got: $actualHash)")
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

                    info.deferred.complete(Unit)
                } catch (e: Throwable) {
                    logger.error("Failed to download $url into ${path.absolutePathString()}!", e)
                    info.deferred.completeExceptionally(e)
                }
            }
        }

        return info
    }
}