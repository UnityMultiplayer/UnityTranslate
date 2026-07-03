package xyz.bluspring.unitytranslate.api.v2

/**
 * A list of languages that are currently known by UnityTranslate.
 * This is not a comprehensive list! It is only a list intended to not require reallocating a
 * language value every time.
 * Note that a language may be reallocated by de/serialization, so make sure you're comparing
 * them correctly.
 */
object Languages {
    val registered: Set<Language>
        field = mutableSetOf()

    // Language code -> Native name references: https://localizely.com/language-code/en/
    @JvmField val AFRIKAANS = register("af")
    @JvmField val ALBANIAN = register("sq")
    @JvmField val ARABIC = register("ar")
    @JvmField val ARMENIAN = register("hy")
    @JvmField val AZERBAIJANI = register("az")
    @JvmField val BASQUE = register("eu")
    @JvmField val BELARUSIAN = register("be")
    @JvmField val BULGARIAN = register("bg")
    @JvmField val BENGALI = register("bn")
    @JvmField val BOSNIAN = register("bs")
    @JvmField val CATALAN = register("ca")
    @JvmField val CHINESE_SIMPLIFIED = register("zh", "Hans")
    @JvmField val CHINESE_TRADITIONAL = register("zh", "Hant")
    @JvmField val CROATIAN = register("hr")
    @JvmField val CZECH = register("cs")
    @JvmField val DANISH = register("da")
    @JvmField val DUTCH = register("nl")
    @JvmField val ESPERANTO = register("eo")
    @JvmField val ESTONIAN = register("et")
    @JvmField val ENGLISH = register("en")
    @JvmField val FINNISH = register("fi")
    @JvmField val FRENCH = register("fr")
    @JvmField val GALICIAN = register("gl")
    @JvmField val GERMAN = register("de")
    @JvmField val GREEK = register("el")
    @JvmField val HEBREW = register("he")
    @JvmField val HINDI = register("hi")
    @JvmField val HUNGARIAN = register("hu")
    @JvmField val ICELANDIC = register("is")
    @JvmField val INDONESIAN = register("id")
    @JvmField val IRISH = register("ga")
    @JvmField val ITALIAN = register("it")
    @JvmField val JAPANESE = register("ja")
    @JvmField val KANNADA = register("kn")
    @JvmField val KAZAKH = register("kk")
    @JvmField val KOREAN = register("ko")
    @JvmField val LATVIAN = register("lv")
    @JvmField val LITHUANIAN = register("lt")
    @JvmField val MACEDONIAN = register("mk")
    @JvmField val MALAY = register("ms")
    @JvmField val MAORI = register("mi")
    @JvmField val MARATHI = register("mr")
    @JvmField val NEPALI = register("ne")
    @JvmField val NORWEGIAN = register("nb")
    @JvmField val PERSIAN = register("fa")
    @JvmField val POLISH = register("pl")
    @JvmField val PORTUGUESE = register("pt", "pt")
    @JvmField val PORTUGUESE_BRAZILIAN = register("pt", "BR")
    @JvmField val ROMANIAN = register("ro")
    @JvmField val RUSSIAN = register("ru")
    @JvmField val SERBIAN = register("sr")
    @JvmField val SLOVAK = register("sk")
    @JvmField val SLOVENIAN = register("sl")
    @JvmField val SPANISH = register("es", "es")
    @JvmField val SPANISH_MEXICAN = register("es", "MX")
    @JvmField val SWAHILI = register("sw")
    @JvmField val SWEDISH = register("sv")
    @JvmField val TAGALOG = register("tl")
    @JvmField val TAMIL = register("ta")
    @JvmField val THAI = register("th")
    @JvmField val TURKISH = register("tr")
    @JvmField val UKRAINIAN = register("uk")
    @JvmField val URDU = register("ur")
    @JvmField val VIETNAMESE = register("vi")
    @JvmField val WELSH = register("cy")

    private fun register(langCode: String, regionCode: String? = null): Language {
        val lang = Language(langCode, regionCode)
        this.registered.add(lang)
        return lang
    }
}
