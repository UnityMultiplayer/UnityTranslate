package xyz.bluspring.unitytranslate.api.v2

import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.api.v2.util.reverse
import net.minecraft.locale.Language as MinecraftLanguage

data class Language(
    val languageCode: String, // ISO 639-1 codes
    val regionCode: String? = null, // ISO 3166-1 codes
) {
    val formatted: String = if (this.regionCode == null)
        this.languageCode
    else
        "${this.languageCode}-${this.regionCode}"

    val serialized: String = if (this.regionCode == null)
        this.languageCode
    else
        "${this.languageCode}_${this.regionCode.lowercase()}"

    val nativeText: String
        get() = MinecraftLanguage.getInstance().getOrDefault("unitytranslate.language.$serialized.native", this.formatted)
    val nativeShortText: String
        get() = MinecraftLanguage.getInstance().getOrDefault("unitytranslate.language.$serialized.native.short", this.nativeText)
    val localizedText: String
        get() = MinecraftLanguage.getInstance().getOrDefault("unitytranslate.language.$serialized.localized", this.formatted)
    val localizedShortText: String
        get() = MinecraftLanguage.getInstance().getOrDefault("unitytranslate.language.$serialized.localized.short", this.nativeText)

    override fun toString(): String = this.formatted

    companion object {
        @JvmField val CODEC: Codec<Language> = Codec.STRING.xmap(Language::parse, Language::formatted)

        @JvmStatic fun codecWithAliasing(aliases: Map<Language, String>): Codec<Language> {
            val reverseLookup = aliases.reverse()

            return Codec.STRING.xmap({
                reverseLookup.getOrElse(it) { parse(it) }
            }, {
                aliases.getOrElse(it) { it.formatted }
            })
        }

        @JvmStatic
        fun parse(code: String): Language {
            if (code.contains("-")) {
                val split = code.split("-")
                return Language(split[0], split[1])
            }

            return Language(code)
        }
    }
}
