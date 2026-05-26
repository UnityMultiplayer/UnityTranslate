package xyz.bluspring.unitytranslate.api.v2.download

import java.net.URI
import java.nio.file.Path

/**
 * Represents a config entry that must be downloaded first before being usable.
 */
interface DownloadableEntry {
    /**
     * The file path to the entry. If it exists and [expectedHash] matches the file's hash, the entry will be made usable.
     */
    val path: Path

    /**
     * The download [java.net.URI] for the entry. When this entry is selected, it will be downloaded first.
     */
    val uri: URI

    /**
     * The expected file hash of the downloaded entry. May be null for an unknown hash.
     */
    val expectedHash: DownloadHash?
        get() = null
}
