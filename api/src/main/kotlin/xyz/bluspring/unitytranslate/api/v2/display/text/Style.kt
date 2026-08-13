package xyz.bluspring.unitytranslate.api.v2.display.text

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs.forGetter
import java.util.*

@JvmRecord
data class Style @JvmOverloads constructor(
    val color: Int? = null,
    val shadow: Int? = null,
    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underlined: Boolean? = null,
    val strikethrough: Boolean? = null,
    val obfuscated: Boolean? = null,
) {
    constructor(
        color: Optional<Int>, shadow: Optional<Int>,
        bold: Optional<Boolean>, italic: Optional<Boolean>,
        underlined: Optional<Boolean>, strikethrough: Optional<Boolean>,
        obfuscated: Optional<Boolean>,
    ) : this(
        color.orElse(null), shadow.orElse(null),
        bold.orElse(null), italic.orElse(null),
        underlined.orElse(null), strikethrough.orElse(null),
        obfuscated.orElse(null),
    )

    fun withColor(color: Int): Style = update(color = color)
    fun withShadow(color: Int): Style = update(shadow = color)
    fun withoutShadow(): Style = update(shadow = 0)
    fun withBold(value: Boolean = true): Style = update(bold = value)
    fun withItalic(value: Boolean = true): Style = update(italic = value)
    fun withUnderlined(value: Boolean = true): Style = update(underlined = value)
    fun withStrikethrough(value: Boolean = true): Style = update(strikethrough = value)
    fun withObfuscated(value: Boolean = true): Style = update(obfuscated = value)

    fun withReset(): Style = EMPTY

    fun merge(other: Style): Style
        = update(
            other.color ?: this.color, other.shadow ?: this.shadow,
            other.bold ?: this.bold, other.italic ?: this.italic,
            other.underlined ?: this.underlined, other.strikethrough ?: this.strikethrough,
            other.obfuscated ?: this.obfuscated
        )

    fun update(
        color: Int? = this.color,
        shadow: Int? = this.shadow,
        bold: Boolean? = this.bold,
        italic: Boolean? = this.italic,
        underlined: Boolean? = this.underlined,
        strikethrough: Boolean? = this.strikethrough,
        obfuscated: Boolean? = this.obfuscated,
    ): Style
        = Style(color, shadow, bold, italic, underlined, strikethrough, obfuscated)

    companion object {
        @JvmField val EMPTY = Style()
        @JvmField val MAP_CODEC: MapCodec<Style> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.optionalFieldOf("color")
                    .forGetter(Style::color),
                Codec.INT.optionalFieldOf("shadow")
                    .forGetter(Style::shadow),
                Codec.BOOL.optionalFieldOf("bold")
                    .forGetter(Style::bold),
                Codec.BOOL.optionalFieldOf("italic")
                    .forGetter(Style::italic),
                Codec.BOOL.optionalFieldOf("underlined")
                    .forGetter(Style::underlined),
                Codec.BOOL.optionalFieldOf("strikethrough")
                    .forGetter(Style::strikethrough),
                Codec.BOOL.optionalFieldOf("obfuscated")
                    .forGetter(Style::obfuscated),
            )
                .apply(instance, ::Style)
        }
        @JvmField val CODEC: Codec<Style> = MAP_CODEC.codec()
    }
}
