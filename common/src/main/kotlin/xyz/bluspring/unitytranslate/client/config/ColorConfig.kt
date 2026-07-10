package xyz.bluspring.unitytranslate.client.config

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs
import xyz.bluspring.unitytranslate.api.v2.util.ColorMatrix
import xyz.bluspring.unitytranslate.client.config.ColorConfig.Gradient.GradientDirection.*

sealed class ColorConfig(val type: String) {
    // must be a 4-sized int array
    abstract val colors: IntArray

    companion object {
        @JvmField val TYPES = listOf("none", "solid", "gradient", "matrix")

        @JvmField
        val CODEC: Codec<ColorConfig> = Codec.STRING.dispatch("type", ColorConfig::type) { type ->
            when (type) {
                "none" -> None.CODEC
                "solid" -> Solid.CODEC
                "gradient" -> Gradient.CODEC
                "matrix" -> Matrix.CODEC
                else -> throw IllegalArgumentException("No color config found by type $type!")
            }
        }

        fun separateMatrix(color: ColorConfig): ColorMatrix {
            val topLeft = color.colors[0]
            val topRight = color.colors[1]
            val bottomLeft = color.colors[2]
            val bottomRight = color.colors[3]

            return ColorMatrix(topLeft, topRight, bottomLeft, bottomRight)
        }
    }

    object None : ColorConfig("none") {
        override val colors: IntArray = intArrayOf(0, 0, 0, 0)

        @JvmField val CODEC: MapCodec<None> = MapCodec.unit(None)
    }

    data class Solid(val color: Int) : ColorConfig("solid") {
        override val colors: IntArray = intArrayOf(color, color, color, color)

        companion object {
            @JvmField
            val CODEC: MapCodec<Solid> = AdditionalCodecs.COLOR_ARGB.fieldOf("color").xmap(::Solid, Solid::color)
        }
    }

    data class Matrix(
        val topLeft: Int, val topRight: Int,
        val bottomLeft: Int, val bottomRight: Int,
    ) : ColorConfig("matrix") {
        override val colors: IntArray = intArrayOf(
            topLeft, topRight,
            bottomLeft, bottomRight
        )

        companion object {
            @JvmField val NAMED_CODEC: Codec<Matrix> = RecordCodecBuilder.create { instance ->
                instance.group(
                    AdditionalCodecs.COLOR_ARGB.fieldOf("top_left")
                        .forGetter(Matrix::topLeft),
                    AdditionalCodecs.COLOR_ARGB.fieldOf("top_right")
                        .forGetter(Matrix::topRight),
                    AdditionalCodecs.COLOR_ARGB.fieldOf("bottom_left")
                        .forGetter(Matrix::bottomLeft),
                    AdditionalCodecs.COLOR_ARGB.fieldOf("bottom_right")
                        .forGetter(Matrix::bottomRight),
                )
                    .apply(instance, ::Matrix)
            }
            @JvmField val ARRAY_CODEC: Codec<Matrix> = AdditionalCodecs.COLOR_ARGB.listOf(4, 4)
                .xmap({ Matrix(it[0], it[1], it[2], it[3]) }, { it.colors.toList() })

            @JvmField val CODEC: MapCodec<Matrix> = Codec.withAlternative(
                NAMED_CODEC, ARRAY_CODEC
            ).fieldOf("colors")
        }
    }

    data class Gradient(val direction: GradientDirection, val fromColor: Int, val toColor: Int) : ColorConfig("gradient") {
        // top left  |  top right  |  bottom left  |  bottom right
        override val colors = when (this.direction) {
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
