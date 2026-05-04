package xyz.bluspring.unitytranslate.standalone

import com.google.gson.JsonParser

@JvmRecord
data class Metadata(
    val version: String,
    val minecraftVersion: String,
    val buildTime: Long,
    val buildHash: String
) {
    companion object {
        private val instance = JsonParser.parseString(this::class.java.getResource("/metadata.json")!!.readText())
            .asJsonObject
            .run {
                Metadata(
                    get("version").asString,
                    get("minecraft_version").asString,
                    get("build_time").asLong,
                    get("build_hash").asString,
                )
            }

        fun get(): Metadata {
            return instance
        }
    }
}