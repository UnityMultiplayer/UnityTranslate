package xyz.bluspring.unitytranslate.config

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gg.essential.elementa.components.GradientComponent
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs

sealed class ColorConfig(val type: String) {
    companion object {
        @JvmField
        val CODEC: Codec<ColorConfig> = Codec.STRING.dispatch("type", ColorConfig::type) { type ->
            when (type) {
                "none" -> None.CODEC
                "solid" -> Solid.CODEC
                "gradient" -> Gradient.CODEC
                else -> throw IllegalArgumentException("No color config found by type $type!")
            }
        }
    }

    object None : ColorConfig("none") {
        @JvmField val CODEC: MapCodec<None> = MapCodec.unit(None)
    }

    data class Solid(val color: Int) : ColorConfig("solid") {
        companion object {
            @JvmField
            val CODEC: MapCodec<Solid> = AdditionalCodecs.COLOR_ARGB.fieldOf("color").xmap(::Solid, Solid::color)
        }
    }

    data class Gradient(val direction: GradientComponent.GradientDirection, val fromColor: Int, val toColor: Int) : ColorConfig("gradient") {
        companion object {
            @JvmField
            val CODEC: MapCodec<Gradient> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    AdditionalCodecs.enumCodec(GradientComponent.GradientDirection::valueOf)
                        .fieldOf("direction")
                        .forGetter(Gradient::direction),
                    AdditionalCodecs.COLOR_ARGB.fieldOf("from_color")
                        .forGetter(Gradient::fromColor),
                    AdditionalCodecs.COLOR_ARGB.fieldOf("to_color")
                        .forGetter(Gradient::toColor)
                )
                    .apply(instance, ::Gradient)
            }
        }
    }
}
