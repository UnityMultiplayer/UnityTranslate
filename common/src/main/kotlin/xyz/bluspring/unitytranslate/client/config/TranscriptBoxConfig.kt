package xyz.bluspring.unitytranslate.client.config

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import gg.essential.elementa.components.GradientComponent
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import org.joml.Vector2f
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs.optionalFieldOf
import xyz.bluspring.unitytranslate.util.Box2f
import xyz.bluspring.unitytranslate.util.ScreenUtil

data class TranscriptBoxConfig(
    val transforms: Transforms,

    val outline: Outline = Outline(),
    var background: Background = Background.default(),
    var textColor: Int = DEFAULT_TEXT_COLOR,
    var shadowColor: Int = DEFAULT_SHADOW_COLOR,
    var fontScale: Float = -1f, // -1 = MC GUI scale
    var cornerRadius: Float = DEFAULT_CORNER_RADIUS,

    var header: Header = Header.default(),
    var transcriptDisplay: TranscriptDisplay = TranscriptDisplay.default(),
) {
    companion object {
        @JvmField val DEFAULT_TEXT_COLOR = ARGBHelper.color(255, 255, 255, 255)
        @JvmField val DEFAULT_SHADOW_COLOR = ARGBHelper.color(255, 0, 0, 0)
        const val DEFAULT_CORNER_RADIUS = 1f

        @JvmField
        val CODEC: Codec<TranscriptBoxConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Transforms.CODEC.fieldOf("transforms")
                    .forGetter(TranscriptBoxConfig::transforms),
                Outline.CODEC.optionalFieldOf("outline", ::Outline)
                    .forGetter(TranscriptBoxConfig::outline),
                Background.CODEC.optionalFieldOf("background", Background::default)
                    .forGetter(TranscriptBoxConfig::background),
                Codec.INT.optionalFieldOf("text_color", DEFAULT_TEXT_COLOR)
                    .forGetter(TranscriptBoxConfig::textColor),
                Codec.INT.optionalFieldOf("shadow_color", DEFAULT_SHADOW_COLOR)
                    .forGetter(TranscriptBoxConfig::shadowColor),
                Codec.FLOAT.optionalFieldOf("font_scale", -1f)
                    .forGetter(TranscriptBoxConfig::fontScale),
                Codec.FLOAT.optionalFieldOf("corner_radius", DEFAULT_CORNER_RADIUS)
                    .forGetter(TranscriptBoxConfig::cornerRadius),
                Header.CODEC.optionalFieldOf("header", Header::default)
                    .forGetter(TranscriptBoxConfig::header),
                TranscriptDisplay.CODEC.optionalFieldOf("transcript_display", TranscriptDisplay::default)
                    .forGetter(TranscriptBoxConfig::transcriptDisplay)
            )
                .apply(instance, ::TranscriptBoxConfig)
        }
    }

    data class Transforms(
        var position: Position,
        var size: Size,
    ) {
        companion object {
            val CODEC: Codec<Transforms> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Position.CODEC.fieldOf("position")
                        .forGetter(Transforms::position),
                    Size.CODEC.fieldOf("size")
                        .forGetter(Transforms::size)
                )
                    .apply(instance, ::Transforms)
            }
        }

        sealed class Position(val type: String) {
            companion object {
                @JvmField val CODEC: Codec<Position> = Codec.STRING.dispatch("type", Position::type) { type ->
                    when (type) {
                        "absolute" -> Absolute.CODEC
                        "relative" -> Relative.CODEC
                        else -> throw IllegalArgumentException("No position anchor exists by type $type!")
                    }
                }
            }

            abstract fun calculatePos(screenWidth: Int, screenHeight: Int): Vector2f

            data class Absolute(var x: Int, var y: Int) : Position("absolute") {
                override fun calculatePos(screenWidth: Int, screenHeight: Int): Vector2f {
                    return Vector2f(
                        this.x.coerceAtMost(screenWidth).toFloat(),
                        this.y.coerceAtMost(screenHeight).toFloat()
                    )
                }

                companion object {
                    @JvmField val CODEC: MapCodec<Absolute> = RecordCodecBuilder.mapCodec { instance ->
                        instance.group(
                            Codec.INT.fieldOf("x")
                                .forGetter(Absolute::x),
                            Codec.INT.fieldOf("y")
                                .forGetter(Absolute::y)
                        )
                            .apply(instance, ::Absolute)
                    }
                }
            }

            data class Relative( // preferred, default.
                var xAlign: Float, // screen %
                var yAlign: Float,
            ) : Position("relative") {
                override fun calculatePos(screenWidth: Int, screenHeight: Int): Vector2f {
                    return Vector2f(this.xAlign * screenWidth, this.yAlign * screenHeight)
                }

                companion object {
                    @JvmField val CODEC: MapCodec<Relative> = RecordCodecBuilder.mapCodec { instance ->
                        instance.group(
                            Codec.FLOAT.fieldOf("align_x")
                                .forGetter(Relative::xAlign),
                            Codec.FLOAT.fieldOf("align_y")
                                .forGetter(Relative::yAlign)
                        )
                            .apply(instance, ::Relative)
                    }
                }
            }
        }

        /*
        Size should build using the determined position as the origin point for the size.
        For instance, if the position on the screen is (0.45, 0.5) and the size is (350px, 200px), the size
        is determined by expanding by 45% of 350px towards the left and 55% of 350px towards the right.
         */
        sealed class Size(val type: String) {
            companion object {
                @JvmField val CODEC: Codec<Size> = Codec.STRING.dispatch("type", Size::type) { type ->
                    when (type) {
                        "absolute" -> Absolute.CODEC
                        "anchored" -> Anchored.CODEC
                        else -> throw IllegalArgumentException("No size anchor exists by type $type!")
                    }
                }
            }

            abstract fun calculateDimensions(screenPos: Vector2f, screenWidth: Int, screenHeight: Int): Box2f

            data class Absolute(var width: Float, var height: Float) : Size("absolute") {
                override fun calculateDimensions(screenPos: Vector2f, screenWidth: Int, screenHeight: Int): Box2f {
                    return Box2f.expandFromAnchor(
                        ScreenUtil.findAnchorPoint(screenPos, this.width, this.height, screenWidth, screenHeight),
                        this.width, this.height
                    )
                }

                companion object {
                    @JvmField val CODEC: MapCodec<Absolute> = RecordCodecBuilder.mapCodec { instance ->
                        instance.group(
                            Codec.FLOAT.fieldOf("width")
                                .forGetter(Absolute::width),
                            Codec.FLOAT.fieldOf("height")
                                .forGetter(Absolute::height)
                        )
                            .apply(instance, ::Absolute)
                    }
                }
            }

            data class Anchored(
                var maxWidth: Float, // px
                var maxHeight: Float,
            ) : Size("anchored") {
                override fun calculateDimensions(
                    screenPos: Vector2f,
                    screenWidth: Int,
                    screenHeight: Int
                ): Box2f {
                    val aspectRatio = this.maxWidth / this.maxHeight
                    val anchor = ScreenUtil.findAnchorPoint(screenPos, this.maxWidth, this.maxHeight, screenWidth, screenHeight)

                    var targetWidth = this.maxWidth
                    var targetHeight = this.maxHeight

                    val expectedDimensions = Box2f.expandFromAnchor(anchor, targetWidth, targetHeight)

                    if (screenPos.y + expectedDimensions.bottom > screenHeight)
                        targetHeight -= (screenHeight - screenPos.y - expectedDimensions.bottom)

                    if (screenPos.y - expectedDimensions.top < 0)
                        targetHeight += (screenPos.y - expectedDimensions.top)

                    if (screenPos.x + expectedDimensions.right > screenWidth)
                        targetWidth -= (screenWidth - screenPos.x - expectedDimensions.right)

                    if (screenPos.x - expectedDimensions.left < 0)
                        targetWidth += (screenPos.x - expectedDimensions.left)

                    if (targetWidth == this.maxWidth && targetHeight == this.maxHeight)
                        return expectedDimensions

                    val widthDiff = targetWidth / this.maxWidth
                    val heightDiff = targetHeight / this.maxHeight

                    // width / height = aspect ratio
                    // width = aspect ratio * height
                    // width / aspect ratio = height

                    if (heightDiff > widthDiff) {
                        // prioritize scaling the height
                        targetWidth = aspectRatio * targetHeight
                    } else {
                        targetHeight = targetWidth / aspectRatio
                    }

                    return Box2f.expandFromAnchor(anchor, targetWidth, targetHeight)
                }

                companion object {
                    @JvmField val CODEC: MapCodec<Anchored> = RecordCodecBuilder.mapCodec { instance ->
                        instance.group(
                            Codec.FLOAT.fieldOf("max_width")
                                .forGetter(Anchored::maxWidth),
                            Codec.FLOAT.fieldOf("max_height")
                                .forGetter(Anchored::maxHeight)
                        )
                            .apply(instance, ::Anchored)
                    }
                }
            }
        }
    }

    data class Outline(
        var color: ColorConfig = DEFAULT_COLOR,
        var thickness: Float = DEFAULT_THICKNESS
    ) {
        companion object {
            @JvmField val DEFAULT_COLOR = ColorConfig.Solid(ARGBHelper.color(200, 0, 0, 0))
            const val DEFAULT_THICKNESS = 1f

            @JvmField
            val CODEC: Codec<Outline> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ColorConfig.CODEC.optionalFieldOf("color", DEFAULT_COLOR)
                        .forGetter(Outline::color),
                    Codec.floatRange(0f, 128f).optionalFieldOf("thickness", DEFAULT_THICKNESS)
                        .forGetter(Outline::thickness)
                )
                    .apply(instance, ::Outline)
            }
        }
    }

    sealed class Background(val type: String) {
        companion object {
            @JvmStatic
            fun default(): Background = Color(
                ColorConfig.Gradient(GradientComponent.GradientDirection.TOP_TO_BOTTOM,
                ARGBHelper.color(128, 0, 0, 0),
                ARGBHelper.color(172, 0, 0, 0)
            ))

            @JvmField
            val CODEC: Codec<Background> = Codec.STRING.dispatch("type", Background::type) { type ->
                when (type) {
                    "color" -> Color.CODEC
                    "image" -> Image.CODEC
                    else -> throw IllegalArgumentException("No background found by type $type!")
                }
            }
        }

        data class Color(var color: ColorConfig) : Background("color") {
            companion object {
                @JvmField
                val CODEC: MapCodec<Color> = ColorConfig.CODEC.fieldOf("color")
                    .xmap(::Color, Color::color)
            }
        }

        data class Image(var path: String) : Background("image") {
            companion object {
                @JvmField
                val CODEC: MapCodec<Image> = Codec.STRING.fieldOf("path")
                    .xmap(::Image, Image::path)
            }
        }
    }

    data class Header(
        var display: HeaderDisplay = HeaderDisplay.LangCodeUppercase,
        var style: Style = DEFAULT_STYLE,
    ) {
        companion object {
            @JvmField val DEFAULT_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(true).withUnderlined(true)
            fun default(): Header = Header()

            @JvmField
            val CODEC: Codec<Header> = RecordCodecBuilder.create { instance ->
                instance.group(
                    HeaderDisplay.CODEC.optionalFieldOf("display", HeaderDisplay.LangCodeUppercase)
                        .forGetter(Header::display),
                    Style.Serializer.CODEC.optionalFieldOf("style", DEFAULT_STYLE)
                        .forGetter(Header::style)
                )
                    .apply(instance, ::Header)
            }
        }
    }

    sealed class HeaderDisplay(val type: String) {
        companion object {
            const val HEADER_LANG = "unitytranslate.transcript.header"

            @JvmField val CODEC: Codec<HeaderDisplay> = Codec.STRING.dispatch("type", HeaderDisplay::type) { type ->
                when (type) {
                    "none" -> None.CODEC
                    "lang_code" -> LangCode.CODEC
                    "lang_code_uppercase" -> LangCodeUppercase.CODEC
                    "lang_name" -> LangName.CODEC
                    "custom" -> Custom.CODEC
                    else -> throw IllegalArgumentException("No header display found by type $type!")
                }
            }
        }

        abstract fun text(languageCode: String): Component

        object None : HeaderDisplay("none") {
            @JvmField val CODEC: MapCodec<None> = MapCodec.unit(None)

            override fun text(languageCode: String): Component = Component.empty()
        }

        object LangCode : HeaderDisplay("lang_code") {
            @JvmField val CODEC: MapCodec<LangCode> = MapCodec.unit(LangCode)

            // Transcript (en)
            override fun text(languageCode: String): Component = Component.translatable(HEADER_LANG, languageCode)
        }

        object LangCodeUppercase : HeaderDisplay("lang_code_uppercase") {
            @JvmField val CODEC: MapCodec<LangCodeUppercase> = MapCodec.unit(LangCodeUppercase)

            // Transcript (EN)
            override fun text(languageCode: String): Component = Component.translatable(HEADER_LANG, languageCode.uppercase())
        }

        object LangName : HeaderDisplay("lang_name") {
            @JvmField val CODEC: MapCodec<LangName> = MapCodec.unit(LangName)

            // Transcript (English)
            override fun text(languageCode: String): Component = Component.translatable(HEADER_LANG, Component.translatableWithFallback("unitytranslate.language.$languageCode", languageCode))
        }

        data class Custom(val text: String) : HeaderDisplay("custom") {
            override fun text(languageCode: String): Component = Component.literal(this.text)

            companion object {
                @JvmField val CODEC: MapCodec<Custom> = Codec.STRING.fieldOf("text")
                    .xmap(::Custom, Custom::text)
            }
        }
    }

    sealed class TranscriptDisplay(val type: String) {
        companion object {
            const val MESSAGE_LANG = "unitytranslate.transcript.message"
            const val LANGUAGE_LANG = "unitytranslate.transcript.message.language"

            @JvmField val DEFAULT_STYLE = Style.EMPTY.withColor(ChatFormatting.GREEN)
            @JvmField val CODEC: Codec<TranscriptDisplay> = Codec.STRING.dispatch("type", TranscriptDisplay::type) { type ->
                when (type) {
                    "lang_code" -> LangCode.CODEC
                    "lang_code_uppercase" -> LangCodeUppercase.CODEC
                    "lang_name" -> LangName.CODEC
                    else -> throw IllegalArgumentException("No transcript display found by type $type!")
                }
            }

            @JvmStatic fun default(): TranscriptDisplay = LangCodeUppercase()
        }

        abstract fun text(data: TranscriptData): Component

        data class LangCode(val style: Style = DEFAULT_STYLE) : TranscriptDisplay("lang_code") {
            // <BluSpring (en)> Hi
            override fun text(data: TranscriptData): Component = Component.translatable(MESSAGE_LANG, data.sender.displayName,
                Component.translatable(LANGUAGE_LANG, data.languageCode).withStyle(this.style),
                data.message
            )

            companion object {
                @JvmField val CODEC: MapCodec<LangCode> = Style.Serializer.CODEC.optionalFieldOf("style", DEFAULT_STYLE)
                    .xmap(::LangCode, LangCode::style)
            }
        }

        data class LangCodeUppercase(val style: Style = DEFAULT_STYLE) : TranscriptDisplay("lang_code_uppercase") {
            // <BluSpring (EN)> Hi
            override fun text(data: TranscriptData): Component = Component.translatable(MESSAGE_LANG, data.sender.displayName,
                Component.translatable(LANGUAGE_LANG, data.languageCode.uppercase()).withStyle(this.style),
                data.message
            )

            companion object {
                @JvmField val CODEC: MapCodec<LangCodeUppercase> = Style.Serializer.CODEC.optionalFieldOf("style", DEFAULT_STYLE)
                    .xmap(::LangCodeUppercase, LangCodeUppercase::style)
            }
        }

        data class LangName(val style: Style = DEFAULT_STYLE) : TranscriptDisplay("lang_code") {
            // <BluSpring (English)> Hi
            override fun text(data: TranscriptData): Component = Component.translatable(MESSAGE_LANG, data.sender.displayName,
                Component.translatable(LANGUAGE_LANG, Component.translatableWithFallback("unitytranslate.language.${data.languageCode}", data.languageCode)).withStyle(this.style),
                data.message
            )

            companion object {
                @JvmField val CODEC: MapCodec<LangName> = Style.Serializer.CODEC.optionalFieldOf("style", DEFAULT_STYLE)
                    .xmap(::LangName, LangName::style)
            }
        }
    }
}
