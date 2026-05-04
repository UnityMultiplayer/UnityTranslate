package xyz.bluspring.unitytranslate.api.v2.download

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.annotations.ApiStatus

/**
 * Provides information towards the current download.
 */
sealed interface DownloadInfo {
    /**
     * Total amount of data downloaded, in bytes.
     */
    val downloadedBytes: Long

    /**
     * Total amount of data to download, in bytes.
     * This may return -1, which indicates that the final file size is unknown.
     */
    val totalBytes: Long

    /**
     * The remaining amount of data left to download, in bytes.
     * This may return -1 if [totalBytes] is -1, which indicates that the final file size is unknown.
     * If [deferred] is completed, this value will automatically return 0.
     */
    val remainingBytes: Long
        get() {
            if (deferred.isCompleted && !deferred.isCancelled)
                return 0

            if (totalBytes < 0)
                return -1

            return totalBytes - downloadedBytes
        }

    /**
     * The current progress of the download, in a value between [0.0,1.0].
     * If [totalBytes] is -1, the progress will be at 0.0 unless [deferred] is completed.
     */
    val progress: Double
        get() {
            if (deferred.isCompleted && !deferred.isCancelled)
                return 1.0

            if (totalBytes < 0)
                return 0.0

            return (downloadedBytes / totalBytes).toDouble()
        }

    /**
     * A [CompletableDeferred] to attach to for automatically detecting when the download is completed.
     * It is highly recommended to use this for detecting a completed download, and only use the other values as information to display to the user.
     */
    val deferred: Deferred<Unit>

    @ApiStatus.Internal
    open class Mutable : DownloadInfo {
        override var downloadedBytes: Long = 0
            internal set

        override var totalBytes: Long = 0
            internal set

        override lateinit var deferred: Deferred<Unit>
            internal set
    }
}
