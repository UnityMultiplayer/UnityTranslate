package xyz.bluspring.unitytranslate.api.v2.util

import com.google.gson.JsonArray
import com.google.gson.JsonObject

object JsonHelper {
    @JvmStatic
    fun JsonObject.getStringOrNull(name: String): String? {
        return this.get(name)?.asString
    }

    @JvmStatic
    fun JsonArray.addAll(values: Collection<String>) {
        for (string in values) {
            this.add(string)
        }
    }
}
