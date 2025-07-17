package xyz.bluspring.unitytranslate.common

import xyz.bluspring.modernnetworking.api.NetworkCodecs
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum

enum class Language(
    val code: String,
    val supportedTranscribers: Map<TranscriberType, String>
) : TranslatableEnum {
    // Any languages that don't have translation support in LibreTranslate or Argos Translate should not be supported here.
    // Use this for reference for the Browser Transcriber: https://r12a.github.io/app-subtags/
    ENGLISH("en", mapOf(
        TranscriberType.SPHINX to "en-us",
        TranscriberType.BROWSER to "en-US",
        TranscriberType.WHISPER to "en"
    )),
    SPANISH("es", mapOf(
        TranscriberType.SPHINX to "es-mx",
        TranscriberType.BROWSER to "es-013",
        TranscriberType.WHISPER to "es"
    )),
    PORTUGUESE("pt", mapOf(
        TranscriberType.SPHINX to "br-pt",
        TranscriberType.BROWSER to "pt-BR",
        TranscriberType.WHISPER to "pt"
    )),
    FRENCH("fr", mapOf(
        TranscriberType.SPHINX to "fr-fr",
        TranscriberType.BROWSER to "fr",
        TranscriberType.WHISPER to "fr"
    )),
    SWEDISH("sv", mapOf(
        TranscriberType.BROWSER to "sv",
        TranscriberType.WHISPER to "sv"
    )),
    MALAY("ms", mapOf(
        TranscriberType.BROWSER to "ms",
        TranscriberType.WHISPER to "ms"
    )),
    HEBREW("he", mapOf(
        TranscriberType.BROWSER to "he",
        TranscriberType.WHISPER to "he"
    )),
    ARABIC("ar", mapOf(
        TranscriberType.BROWSER to "ar",
        TranscriberType.WHISPER to "ar"
    )),
    GERMAN("de", mapOf(
        TranscriberType.BROWSER to "de",
        TranscriberType.WHISPER to "de"
    )),
    RUSSIAN("ru", mapOf(
        TranscriberType.BROWSER to "ru",
        TranscriberType.WHISPER to "ru"
    )),
    JAPANESE("ja", mapOf(
        TranscriberType.BROWSER to "ja",
        TranscriberType.WHISPER to "ja"
    )),
    CHINESE("zh", mapOf(
        TranscriberType.BROWSER to "cmn",
        TranscriberType.WHISPER to "zh"
    )),
    ITALIAN("it", mapOf(
        TranscriberType.BROWSER to "it",
        TranscriberType.WHISPER to "it"
    )),
    CHINESE_TRADITIONAL("zt", mapOf(
        TranscriberType.BROWSER to "cmn", // TODO: ???
        TranscriberType.WHISPER to "zh"
    )),
    CZECH("cs", mapOf(
        TranscriberType.BROWSER to "cs",
        TranscriberType.WHISPER to "cs"
    )),
    DANISH("da", mapOf(
        TranscriberType.BROWSER to "da",
        TranscriberType.WHISPER to "da"
    )),
    DUTCH("nl", mapOf(
        TranscriberType.BROWSER to "nl",
        TranscriberType.WHISPER to "nl"
    )),
    FINNISH("fi", mapOf(
        TranscriberType.BROWSER to "fi",
        TranscriberType.WHISPER to "fi"
    )),
    GREEK("el", mapOf(
        TranscriberType.BROWSER to "el",
        TranscriberType.WHISPER to "el"
    )),
    HINDI("hi", mapOf(
        TranscriberType.BROWSER to "hi",
        TranscriberType.WHISPER to "hi"
    )),
    HUNGARIAN("hu", mapOf(
        TranscriberType.BROWSER to "hu",
        TranscriberType.WHISPER to "hu"
    )),
    INDONESIAN("id", mapOf(
        TranscriberType.BROWSER to "id",
        TranscriberType.WHISPER to "id"
    )),
    KOREAN("ko", mapOf(
        TranscriberType.BROWSER to "ko",
        TranscriberType.WHISPER to "ko"
    )),
    NORWEGIAN("nb", mapOf(
        TranscriberType.BROWSER to "nb",
        TranscriberType.WHISPER to "nb"
    )),
    POLISH("pl", mapOf(
        TranscriberType.BROWSER to "pl",
        TranscriberType.WHISPER to "pl"
    )),
    TAGALOG("tl", mapOf(
        TranscriberType.BROWSER to "tl",
        TranscriberType.WHISPER to "tl"
    )),
    THAI("th", mapOf(
        TranscriberType.BROWSER to "th",
        TranscriberType.WHISPER to "th"
    )),
    TURKISH("tr", mapOf(
        TranscriberType.BROWSER to "tr",
        TranscriberType.WHISPER to "tr"
    )),
    UKRAINIAN("uk", mapOf(
        TranscriberType.BROWSER to "uk",
        TranscriberType.WHISPER to "uk"
    )),
    BULGARIAN("bg", mapOf(
        TranscriberType.BROWSER to "bg",
        TranscriberType.WHISPER to "bg"
    )),
    ALBANIAN("sq", mapOf(
        TranscriberType.BROWSER to "sq"
    )),
    AZERBAIJANI("az", mapOf(
        TranscriberType.BROWSER to "az",
        TranscriberType.WHISPER to "az"
    )),
    BENGALI("bn", mapOf(
        TranscriberType.BROWSER to "bn"
    )),
    CATALAN("ca", mapOf(
        TranscriberType.BROWSER to "ca",
        TranscriberType.WHISPER to "ca"
    )),
    ESPERANTO("eo", mapOf(
        TranscriberType.BROWSER to "eo"
    )),
    ESTONIAN("et", mapOf(
        TranscriberType.BROWSER to "et",
        TranscriberType.WHISPER to "et"
    )),
    IRISH("ga", mapOf(
        TranscriberType.BROWSER to "ga"
    )),
    LATVIAN("lv", mapOf(
        TranscriberType.BROWSER to "lv",
        TranscriberType.WHISPER to "lv"
    )),
    LITHUANIAN("lt", mapOf(
        TranscriberType.BROWSER to "lt",
        TranscriberType.WHISPER to "lt"
    )),
    PERSIAN("fa", mapOf(
        TranscriberType.BROWSER to "fa",
        TranscriberType.WHISPER to "fa"
    )),
    ROMANIAN("ro", mapOf(
        TranscriberType.BROWSER to "ro",
        TranscriberType.WHISPER to "ro"
    )),
    SLOVAK("sk", mapOf(
        TranscriberType.BROWSER to "sk",
        TranscriberType.WHISPER to "sk"
    )),
    SLOVENIAN("sl", mapOf(
        TranscriberType.BROWSER to "sl",
        TranscriberType.WHISPER to "sl"
    )),
    URDU("ur", mapOf(
        TranscriberType.BROWSER to "ur",
        TranscriberType.WHISPER to "ur"
    ));

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
