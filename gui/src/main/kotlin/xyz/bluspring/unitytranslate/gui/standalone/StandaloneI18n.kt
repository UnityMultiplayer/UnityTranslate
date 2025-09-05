package xyz.bluspring.unitytranslate.gui.standalone

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import gg.essential.universal.UI18n
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui

class StandaloneI18n : UI18n.Provider {
    override fun i18n(key: String, vararg arguments: Any?): String {
        return String.format(if (json.has(key))
            json.get(key).asString
        else key, *arguments)
    }

    companion object {
        private lateinit var json: JsonObject

        fun loadLanguage(code: String) {
            val language = UnityTranslateGui::class.java.getResource("/assets/unitytranslate/lang/$code.json") ?: throw IllegalArgumentException("Language $code does not exist!")
            json = JsonParser.parseString(language.readText()).asJsonObject
        }
    }
}