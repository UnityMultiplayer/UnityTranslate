package xyz.bluspring.unitytranslate.api.v2

interface LanguageSupporter {
    suspend fun supportsLanguage(language: Language): Boolean

    /**
     * Checks if this transcriber generally supports the language, whether natively supporting the language code and its dialect or simply supporting the language in general.
     */
    suspend fun checkLanguageSupport(language: Language): Language.SupportLevel {
        if (this.supportsLanguage(language))
            return Language.SupportLevel.FULL // Supports the language directly

        if (language.regionCode != null && this.supportsLanguage(language.withoutRegion))
            return Language.SupportLevel.PARTIAL

        return Language.SupportLevel.NONE
    }

    suspend fun getEffectiveLanguage(language: Language): Language? {
        if (this.supportsLanguage(language))
            return language

        if (language.regionCode != null && this.supportsLanguage(language.withoutRegion))
            return language.withoutRegion

        return null
    }
}
