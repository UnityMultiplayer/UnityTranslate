package xyz.bluspring.unitytranslate.api.v2.display.text

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import java.util.*

sealed interface ComponentContents {
    val text: String

    @JvmRecord
    data class Literal(override val text: String) : ComponentContents {
        companion object {
            @JvmField val CODEC: MapCodec<Literal> = Codec.STRING.fieldOf("text").xmap(::Literal, Literal::text)
        }
    }

    @JvmRecord
    data class Translatable(val key: String, val args: List<Any?>, val fallback: Optional<String> = Optional.empty()) : ComponentContents {
        constructor(key: String, args: List<Any?>, fallback: String) : this(key, args, Optional.of(fallback))

        override val text: String
            get() =
                if (this.fallback.isPresent)
                    UnityTranslateApi.instance.platform.translatedWithFallback(key, fallback.orElseThrow(), *this.args.toTypedArray())
                else
                    UnityTranslateApi.instance.platform.translated(key, *this.args.toTypedArray())

        companion object {
            @JvmField val CODEC: MapCodec<Translatable> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.STRING.fieldOf("translate")
                        .forGetter(Translatable::key),
                    Codec.STRING.listOf().optionalFieldOf("with", emptyList())
                        .xmap<List<Any?>>({ it }, { it.map { b -> b.toString() } })
                        .forGetter(Translatable::args),
                    Codec.STRING.optionalFieldOf("fallback")
                        .forGetter(Translatable::fallback),
                )
                    .apply(instance, ::Translatable)
            }
        }
    }
}
