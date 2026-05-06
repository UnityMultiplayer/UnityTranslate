package xyz.bluspring.unitytranslate.api.v2.download

import java.security.MessageDigest

abstract class DownloadHash(val type: String, val hash: String) {
    open fun createDigest(): MessageDigest = MessageDigest.getInstance(this.type)

    class Sha1(hash: String) : DownloadHash("SHA-1", hash)
    class Sha256(hash: String) : DownloadHash("SHA-256", hash)
    class Sha512(hash: String) : DownloadHash("SHA-512", hash)
    class Md5(hash: String) : DownloadHash("MD5", hash)
}