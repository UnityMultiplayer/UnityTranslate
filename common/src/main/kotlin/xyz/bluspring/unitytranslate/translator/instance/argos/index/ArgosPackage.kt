package xyz.bluspring.unitytranslate.translator.instance.index

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.Languages

@JvmRecord
data class ArgosPackage(
    val packageVersion: String,
    val argosVersion: String,

    override val from: Language,
    override val to: Language,

    val links: List<String>,
    override val code: String,
) : ModelPackage {
    companion object {
        // Argos' index uses unofficial codes, so we need to remap them.
        // This is based on https://github.com/LibreTranslate/LibreTranslate/blob/main/libretranslate/language.py#L9
        private val aliases = mapOf(
            Languages.PORTUGUESE_BRAZILIAN to "pb",
            Languages.CHINESE_SIMPLIFIED to "zh",
            Languages.CHINESE_TRADITIONAL to "zt",
        )

        @JvmField val CODEC: Codec<ArgosPackage> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("package_version")
                    .forGetter(ArgosPackage::packageVersion),
                Codec.STRING.fieldOf("argos_version")
                    .forGetter(ArgosPackage::argosVersion),
                Language.codecWithAliasing(aliases).fieldOf("from_code")
                    .forGetter(ArgosPackage::from),
                Language.codecWithAliasing(aliases).fieldOf("to_code")
                    .forGetter(ArgosPackage::to),
                Codec.STRING.listOf().fieldOf("links")
                    .forGetter(ArgosPackage::links),
                Codec.STRING.fieldOf("code")
                    .forGetter(ArgosPackage::code)
            )
                .apply(instance, ::ArgosPackage)
        }
    }
}
