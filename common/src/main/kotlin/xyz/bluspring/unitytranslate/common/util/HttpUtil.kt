package xyz.bluspring.unitytranslate.common.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.http.HttpHeaders
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import xyz.bluspring.unitytranslate.common.UnityTranslate
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL

object HttpUtil {
    val availablePort: Int
        get() {
            return try {
                ServerSocket(0).use { serverSocket ->
                    return@use serverSocket.localPort
                }
            } catch (_: Throwable) {
                25564
            }
        }

    fun isPortAvailable(port: Int): Boolean {
        return if (port >= 0 && port <= 65535) {
            try {
                ServerSocket(port).use {
                    return@use it.localPort == port
                }
            } catch (_: Throwable) {
                false
            }
        } else false
    }

    private var hasApache: Boolean? = null

    fun post(uri: String, body: JsonObject, headers: Map<String, String> = mapOf()): JsonElement {
        if (hasApache == null) {
            try {
                // This is literally only here because of Forge.
                Class.forName("org.apache.http.impl.client.HttpClients")
                Class.forName("org.apache.http.HttpHeaders")
                Class.forName("org.apache.http.util.EntityUtils")
                Class.forName("org.apache.commons.logging.LogFactory")
                hasApache = true
            } catch (e: Throwable) {
                UnityTranslate.logger.error("Detected that Apache HTTP support is unavailable! Translations may fail after a certain period of time.")
                UnityTranslate.logger.error("For more information, view: https://github.com/BluSpring/UnityTranslate/issues/4")
                e.printStackTrace()
                hasApache = false
            }
        }

        return if (hasApache == true) {
            postApache(uri, body, headers)
        } else {
            postJava(uri, body, headers)
        }
    }

    fun postJava(uri: String, body: JsonObject, headers: Map<String, String> = mapOf()): JsonElement {
        val url = URL(uri)
        var connection: HttpURLConnection? = null

        val data = body.toString().toByteArray(Charsets.UTF_8)

        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 60_000
            connection.readTimeout = 60_000

            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            connection.doOutput = true
            connection.setFixedLengthStreamingMode(data.size)

            val outputStream = BufferedOutputStream(connection.outputStream)

            outputStream.write(data)
            outputStream.flush()
            outputStream.close()

            if (connection.responseCode / 100 != 2) {
                throw Exception("Failed to load $uri (code: ${connection.responseCode})")
            } else {
                val inputStream = BufferedInputStream(connection.inputStream)
                val reader = inputStream.bufferedReader(Charsets.UTF_8)
                val result = JsonParser.parseReader(reader)

                reader.close()
                inputStream.close()

                return result
            }
        } finally {
            connection?.disconnect()
        }
    }

    fun postApache(uri: String, body: JsonObject, headers: Map<String, String> = mapOf()): JsonElement {
        HttpClients.createDefault().use { httpClient ->
            val request = HttpPost(uri)
            request.config = RequestConfig.custom()
                .setConnectTimeout(60_000)
                .setSocketTimeout(60_000)
                .setCookieSpec(CookieSpecs.STANDARD)
                .setConnectionRequestTimeout(60_000)
                .build()

            request.setHeader(HttpHeaders.ACCEPT, "application/json")
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            for ((key, value) in headers) {
                request.setHeader(key, value)
            }

            request.entity = StringEntity(body.toString())

            val response = httpClient.execute(request)

            if (response.statusLine.statusCode / 100 != 2)
                throw Exception("Failed to load $uri (code: ${response.statusLine.statusCode})")

            val responseBody = EntityUtils.toString(response.entity, Charsets.UTF_8)

            return JsonParser.parseString(responseBody)
        }
    }
}