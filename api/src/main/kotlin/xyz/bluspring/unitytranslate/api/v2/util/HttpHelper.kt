package xyz.bluspring.unitytranslate.api.v2.util

import java.net.HttpURLConnection
import java.net.URL

/**
 * A couple helper functions used for handling HTTP connections
 */
object HttpHelper {
    @JvmStatic
    fun URL.requestContentLength(): Long {
        val connection = this.openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        return connection.contentLengthLong
    }
}
