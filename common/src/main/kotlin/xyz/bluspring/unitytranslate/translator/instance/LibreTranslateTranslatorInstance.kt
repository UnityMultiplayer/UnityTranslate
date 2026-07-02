package xyz.bluspring.unitytranslate.translator.instance

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

object LibreTranslateTranslatorInstance : TranslatorInstance() {
    private val logger = LoggerFactory.getLogger(LibreTranslateTranslatorInstance::class.java)
    private val CHECK_INTERVAL = 30.minutes.inWholeMilliseconds
    var entries = mutableListOf<Entry>()

    data class Entry(val url: String, val authKey: String? = null) {
        constructor(url: String, authKey: Optional<String>) : this(url, authKey.orElse(null))

        private var lastCheck = -1L
        private val cachedSupportedLanguages = mutableListOf<LangPair>()
        private var isAvailable = false

        private val authKeyOpt: Optional<String> = Optional.ofNullable(this.authKey)

        suspend fun getSupportedLanguages(): Collection<LangPair> {
            if (System.currentTimeMillis() - this.lastCheck >= CHECK_INTERVAL) {
                try {
                    val url = URL("${this.url}/languages")
                    val json = withContext(Dispatchers.IO) {
                        url.openStream()
                    }.use { JsonParser.parseReader(it.bufferedReader()) }.asJsonArray

                    synchronized(this.cachedSupportedLanguages) {
                        this.cachedSupportedLanguages.clear()
                        for (element in json) {
                            val langObj = element.asJsonObject

                            val fromCode = langObj.get("code").asString

                            if (langObj.has("targets")) {
                                for (ele in langObj.getAsJsonArray("targets")) {
                                    val toCode = ele.asString
                                    this.cachedSupportedLanguages.add(LangPair(Language.parse(fromCode), Language.parse(toCode)))
                                }
                            }
                        }
                    }

                    this.isAvailable = true
                } catch (e: Throwable) {
                    this.isAvailable = false
                    logger.error("Failed to load LibreTranslate URL \"${this.url}/languages\"!", e)
                }

                this.lastCheck = System.currentTimeMillis()
            }

            if (!this.isAvailable)
                return emptyList()

            synchronized(this.cachedSupportedLanguages) {
                return this.cachedSupportedLanguages
            }
        }

        companion object {
            val CODEC: Codec<Entry> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("url")
                        .forGetter(Entry::url),
                    Codec.STRING.optionalFieldOf("auth_key")
                        .forGetter(Entry::authKeyOpt)
                )
                    .apply(instance, ::Entry)
            }
        }
    }

    override suspend fun isAvailable(): Boolean = this.entries.any { it.getSupportedLanguages().isNotEmpty() }

    override suspend fun supportsLanguage(langPair: LangPair): Boolean {
        return this.entries.any { it.getSupportedLanguages().contains(langPair) }
    }

    override suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String> {
        val textArray = JsonArray().apply {
            for (string in text) {
                this.add(string)
            }
        }

        for (entry in this.entries) {
            // Ignore the ones that don't support it.
            if (!entry.getSupportedLanguages().contains(langPair))
                continue

            try {
                val reqJson = JsonObject()
                reqJson.addProperty("source", langPair.from.formatted)
                reqJson.addProperty("target", langPair.to.formatted)
                reqJson.add("q", textArray)

                if (entry.authKey?.isNotBlank() == true)
                    reqJson.addProperty("api_key", entry.authKey)

                val client = HttpClient.newHttpClient()
                val request = HttpRequest.newBuilder(URI.create(entry.url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(1.minutes.toJavaDuration())
                    .POST(HttpRequest.BodyPublishers.ofString(reqJson.toString()))
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.send(request, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
                }

                if (response.statusCode() / 100 != 2)
                    throw Exception("Got status code ${response.statusCode()} with message \"${response.body()}\"!")

                val responseJson = JsonParser.parseString(response.body()).asJsonObject

                return responseJson.getAsJsonArray("translatedText").map {
                    it.asString
                }
            } catch (e: Throwable) {
                logger.error("LibreTranslate instance ${entry.url} failed to translate texts (${text.joinToString(", ") { "\"it\"" }}) from ${langPair.from} -> ${langPair.to}!", e)
            }
        }

        // We shouldn't be able to reach this point, but warn anyway.
        logger.warn("Failed to translate texts (${text.joinToString(", ") { "\"it\"" }}) from ${langPair.from} -> ${langPair.to}! Not sure what happened here.")
        return text
    }
}
