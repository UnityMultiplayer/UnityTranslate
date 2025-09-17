package xyz.bluspring.unitytranslate.common

import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum

enum class Language(
    val code: String
) : TranslatableEnum {
    // Any languages that don't have translation support in LibreTranslate or Argos Translate should not be supported here.
    ENGLISH("en"),
    SPANISH("es"),
    PORTUGUESE("pt"),
    FRENCH("fr"),
    SWEDISH("sv"),
    MALAY("ms"),
    HEBREW("he"),
    ARABIC("ar"),
    GERMAN("de"),
    RUSSIAN("ru"),
    JAPANESE("ja"),
    CHINESE("zh"),
    ITALIAN("it"),
    CHINESE_TRADITIONAL("zt"),
    CZECH("cs"),
    DANISH("da"),
    DUTCH("nl"),
    FINNISH("fi"),
    GREEK("el"),
    HINDI("hi"),
    HUNGARIAN("hu"),
    INDONESIAN("id"),
    KOREAN("ko"),
    NORWEGIAN("nb"),
    POLISH("pl"),
    TAGALOG("tl"),
    THAI("th"),
    TURKISH("tr"),
    UKRAINIAN("uk"),
    BULGARIAN("bg"),
    ALBANIAN("sq"),
    AZERBAIJANI("az"),
    BENGALI("bn"),
    CATALAN("ca"),
    ESPERANTO("eo"),
    ESTONIAN("et"),
    IRISH("ga"),
    LATVIAN("lv"),
    LITHUANIAN("lt"),
    PERSIAN("fa"),
    ROMANIAN("ro"),
    SLOVAK("sk"),
    SLOVENIAN("sl"),
    URDU("ur");

    override fun toString(): String {
        return "$name ($code)"
    }

    override val translationKey = "unitytranslate.language.$code"

    companion object {
        val NETWORK_CODEC = NetworkCodecs.enumCodec(Language::class.java)

        fun findLibreLang(code: String): Language? {
            return Language.entries.firstOrNull { it.code == code }
        }
    }
}
