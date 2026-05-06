package xyz.bluspring.unitytranslate.standalone.launch

import com.google.gson.JsonParser

object LauncherMeta {
    private val json = JsonParser.parseString(this::class.java.getResource("/launcher_meta.json")!!.readText()).asJsonObject

    val version: String = json.get("version").asString
    val javaVersion: String = json.get("java_version").asString
    val buildHash: String = json.get("build_hash").asString
}
