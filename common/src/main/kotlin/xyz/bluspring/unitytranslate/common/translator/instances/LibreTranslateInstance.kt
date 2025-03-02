package xyz.bluspring.unitytranslate.common.translator.instances

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.HttpUtil
import java.net.URL

open class LibreTranslateInstance(instance: UnityTranslate, val url: String, var weight: Int, val authKey: String? = null) : TranslationInstance(instance) {
    private var cachedSupportedLanguages = HashMultimap.create<Language, Language>()

    override val supportedLanguages: Multimap<Language, Language>
        get() {
            if (cachedSupportedLanguages.isEmpty) {
                val array = JsonParser.parseString(URL("$url/languages").readText()).asJsonArray

                for (element in array) {
                    val langData = element.asJsonObject
                    val srcLang = Language.findLibreLang(langData.get("code").asString) ?: continue

                    val targets = langData.getAsJsonArray("targets")
                    for (target in targets) {
                        val targetLang = Language.findLibreLang(target.asString) ?: continue
                        cachedSupportedLanguages.put(srcLang, targetLang)
                    }
                }
            }

            return cachedSupportedLanguages
        }

    open fun detectLanguage(text: String): Language? {
        val detected = HttpUtil.post("$url/detect", JsonObject().apply {
            addProperty("q", text)

            if (authKey?.isNotBlank() == true)
                addProperty("api_key", authKey)
        }).asJsonArray.sortedByDescending { it.asJsonObject.get("confidence").asDouble }

        val langCode = detected.firstOrNull()?.asJsonObject?.get("language")?.asString ?: return null
        val lang = Language.findLibreLang(langCode)

        if (lang == null) {
            UnityTranslate.logger.error("Failed to find language for LibreTranslate code $langCode!")
        }

        return lang
    }

    override suspend fun batchTranslate(from: String, to: String, request: List<String>): List<String> {
        markTranslating()
        val translated = HttpUtil.post("$url/translate", JsonObject().apply {
            addProperty("source", from)
            addProperty("target", to)
            add("q", JsonArray().apply {
                for (s in request) {
                    this.add(s)
                }
            })

            if (authKey?.isNotBlank() == true)
                addProperty("api_key", authKey)
        }).asJsonObject.get("translatedText").asJsonArray
        unmarkTranslating()

        return translated.map { it.asString }
    }

    override suspend fun translate(from: String, to: String, request: String): String {
        markTranslating()
        return HttpUtil.post("$url/translate", JsonObject().apply {
            addProperty("source", from)
            addProperty("target", to)
            addProperty("q", request)
            addProperty("format", "text")

            if (authKey?.isNotBlank() == true)
                addProperty("api_key", authKey)
        })
            .asJsonObject.get("translatedText").asString
            .apply {
                unmarkTranslating()
            }
    }
}