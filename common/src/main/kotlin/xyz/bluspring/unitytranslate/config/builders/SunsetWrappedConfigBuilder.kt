package xyz.bluspring.unitytranslate.config.builders

import com.mojang.serialization.Codec
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.config.ConfigButtonBuilder
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import xyz.bluspring.unitytranslate.config.values.*
import kotlin.reflect.KMutableProperty

class SunsetWrappedConfigBuilder(val wrapped: SunsetConfig.CategoryBuilder) : ConfigBuilder {
    internal var validator: () -> Boolean = { true }

    override fun category(
        id: String,
        builder: ConfigBuilder.() -> Unit
    ) {
        this.wrapped.category(id) {
            SunsetWrappedConfigBuilder(this)
        }
    }

    override fun validator(validator: () -> Boolean) {
        this.validator = validator
    }

    override fun <T> value(
        id: String,
        codec: Codec<T>,
        property: KMutableProperty<T>,
        owner: Any?,
        builder: ConfigValueBuilder<T>.() -> Unit
    ) {
        this.wrapped.custom(
            ValidatingReflectingConfigValue(
                id,
                codec,
                property,
                owner,
                ConfigValueBuilderImpl<T>(id).apply(builder)
            )
        )
    }

    override fun <T> listValue(
        id: String,
        codec: Codec<T>,
        property: KMutableProperty<MutableList<T>>,
        owner: Any?,
        builder: ConfigValueBuilder<MutableList<T>>.() -> Unit
    ) {
        this.wrapped.custom(
            ValidatingReflectingListConfigValue(
                id,
                codec,
                property,
                owner,
                ConfigValueBuilderImpl<MutableList<T>>(id).apply(builder)
            )
        )
    }

    override fun integer(
        id: String,
        min: Int,
        max: Int,
        step: Int,
        property: KMutableProperty<Int>,
        owner: Any?,
        builder: ConfigValueBuilder<Int>.() -> Unit
    ) {
        this.wrapped.custom(
            ValidatingRangedConfigValue(
                id,
                Codec.intRange(min, max),
                property,
                owner,
                min,
                max,
                step,
                ConfigValueBuilderImpl<Int>(id).apply(builder)
            )
        )
    }

    override fun boolean(
        id: String,
        property: KMutableProperty<Boolean>,
        owner: Any?,
        builder: ConfigValueBuilder<Boolean>.() -> Unit
    ) {
        this.wrapped.custom(
            ValidatingReflectingConfigValue(
                id,
                Codec.BOOL,
                property,
                owner,
                ConfigValueBuilderImpl<Boolean>(id).apply(builder)
            )
        )
    }

    override fun float(
        id: String,
        min: Float,
        max: Float,
        step: Float,
        property: KMutableProperty<Float>,
        owner: Any?,
        builder: ConfigValueBuilder<Float>.() -> Unit
    ) {
        this.wrapped.custom(
            ValidatingRangedConfigValue(
                id,
                Codec.floatRange(min, max),
                property,
                owner,
                min,
                max,
                step,
                ConfigValueBuilderImpl<Float>(id).apply(builder)
            )
        )
    }

    override fun double(
        id: String,
        min: Double,
        max: Double,
        step: Double,
        property: KMutableProperty<Double>,
        owner: Any?,
        builder: ConfigValueBuilder<Double>.() -> Unit
    ) {
        this.wrapped.custom(
            ValidatingRangedConfigValue(
                id,
                Codec.doubleRange(min, max),
                property,
                owner,
                min,
                max,
                step,
                ConfigValueBuilderImpl<Double>(id).apply(builder)
            )
        )
    }

    override fun <T> dropdown(
        id: String,
        values: Collection<T>,
        codec: Codec<T>,
        property: KMutableProperty<T>,
        owner: Any?,
        builder: ConfigValueBuilder<T>.() -> Unit
    ) {
        this.wrapped.custom(
            DropdownValidatingReflectingConfigValue(
                id,
                values,
                codec,
                property,
                owner,
                ConfigValueBuilderImpl<T>(id).apply(builder)
            )
        )
    }

    override fun <T : DownloadableEntry> downloadableDropdown(
        id: String,
        values: Collection<T>,
        codec: Codec<T>,
        property: KMutableProperty<T>,
        owner: Any?,
        builder: ConfigValueBuilder<T>.() -> Unit
    ) {
        this.wrapped.custom(
            DownloadableDropdownValidatingReflectingConfigValue(
                id,
                values,
                codec,
                property,
                owner,
                ConfigValueBuilderImpl<T>(id).apply(builder)
            )
        )
    }

    override fun button(id: String, builder: ConfigButtonBuilder.() -> Unit) {
        this.wrapped.custom(
            ButtonConfigValue(id, ConfigButtonBuilderImpl(id).apply(builder))
        )
    }

    override fun separator() {
        this.wrapped.custom(SeparatorConfigValue())
    }

    override fun label(id: String, builder: ConfigValueBuilder<Unit>.() -> Unit) {
        this.wrapped.custom(UnitConfigValue(id, ConfigValueBuilderImpl<Unit>(id).apply(builder)))
    }
}
