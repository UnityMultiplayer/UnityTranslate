package xyz.bluspring.unitytranslate.client.config

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs
import xyz.bluspring.unitytranslate.client.config.ColorConfig.Gradient.GradientDirection.*

sealed class ColorConfig(val type: String) {
    companion object {
        @JvmField val TYPES = listOf("none", "solid", "gradient")

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

    data class Gradient(val direction: GradientDirection, val fromColor: Int, val toColor: Int) : ColorConfig("gradient") {
        // top left  |  top right  |  bottom left  |  bottom right
        val colors = when (this.direction) {
            TOP_LEFT -> intArrayOf(
                toColor, ARGBHelper.srgbLerp(toColor, fromColor, 0.5f),
                ARGBHelper.srgbLerp(toColor, fromColor, 0.5f), fromColor
            )
            TOP -> intArrayOf(
                toColor, toColor,
                fromColor, fromColor
            )
            TOP_RIGHT -> intArrayOf(
                ARGBHelper.srgbLerp(fromColor, toColor, 0.5f), toColor,
                fromColor, ARGBHelper.srgbLerp(fromColor, toColor, 0.5f)
            )
            LEFT -> intArrayOf(
                toColor, fromColor,
                toColor, fromColor
            )
            RIGHT -> intArrayOf(
                fromColor, toColor,
                fromColor, toColor
            )
            BOTTOM_LEFT -> intArrayOf(
                fromColor, ARGBHelper.srgbLerp(fromColor, toColor, 0.5f),
                toColor, ARGBHelper.srgbLerp(fromColor, toColor, 0.5f)
            )
            BOTTOM -> intArrayOf(
                fromColor, fromColor,
                toColor, toColor,
            )
            BOTTOM_RIGHT -> intArrayOf(
                fromColor, ARGBHelper.srgbLerp(fromColor, toColor, 0.5f),
                ARGBHelper.srgbLerp(fromColor, toColor, 0.5f), toColor
            )
        }

        companion object {
            @JvmField
            val CODEC: MapCodec<Gradient> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    AdditionalCodecs.enumCodec(GradientDirection::valueOf)
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

        enum class GradientDirection {
            TOP_LEFT,    TOP,    TOP_RIGHT,
            LEFT,                RIGHT,
            BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
        }
    }
}
