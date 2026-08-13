package xyz.bluspring.unitytranslate.api.v2

interface PlatformAccess {
    fun translated(key: String, vararg args: Any?): String
    fun translatedWithFallback(key: String, fallback: String, vararg args: Any?): String
}
