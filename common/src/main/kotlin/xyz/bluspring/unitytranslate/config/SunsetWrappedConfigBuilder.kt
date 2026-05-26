package xyz.bluspring.unitytranslate.config

import com.mojang.serialization.Codec
import xyz.bluspring.sunset.SunsetConfig
import xyz.bluspring.unitytranslate.api.v2.config.ConfigBuilder
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import kotlin.reflect.KMutableProperty

class SunsetWrappedConfigBuilder(val wrapped: SunsetConfig.CategoryBuilder) : ConfigBuilder {
    override fun category(
        id: String,
        builder: ConfigBuilder.() -> Unit
    ) {
        this.wrapped.category(id) {
            SunsetWrappedConfigBuilder(this)
        }
    }

    override fun <T> value(
        id: String,
        codec: Codec<T>,
        property: KMutableProperty<T>,
        owner: Any?,
        builder: ConfigValueBuilder<T>.() -> Unit
    ) {
        this.wrapped.custom(ValidatingReflectingConfigValue(id, codec, property, owner, ConfigValueBuilder(id)))
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
        this.wrapped.custom(ValidatingRangedConfigValue(id, Codec.intRange(min, max), property, owner, min, max, step, ConfigValueBuilder(id)))
    }

    override fun boolean(
        id: String,
        property: KMutableProperty<Boolean>,
        owner: Any?,
        builder: ConfigValueBuilder<Boolean>.() -> Unit
    ) {
        this.wrapped.custom(ValidatingReflectingConfigValue(id, Codec.BOOL, property, owner, ConfigValueBuilder(id)))
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
        this.wrapped.custom(ValidatingRangedConfigValue(id, Codec.floatRange(min, max), property, owner, min, max, step, ConfigValueBuilder(id)))
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
        this.wrapped.custom(ValidatingRangedConfigValue(id, Codec.doubleRange(min, max), property, owner, min, max, step, ConfigValueBuilder(id)))
    }

    override fun <T> dropdown(
        id: String,
        values: Collection<T>,
        codec: Codec<T>,
        property: KMutableProperty<T>,
        owner: Any?,
        builder: ConfigValueBuilder<T>.() -> Unit
    ) {
        this.wrapped.custom(DropdownValidatingReflectingConfigValue(id, values, codec, property, owner, ConfigValueBuilder(id)))
    }

    override fun <T : DownloadableEntry> downloadableDropdown(
        id: String,
        values: Collection<T>,
        codec: Codec<T>,
        property: KMutableProperty<T>,
        owner: Any?,
        builder: ConfigValueBuilder<T>.() -> Unit
    ) {
        this.wrapped.custom(DownloadableDropdownValidatingReflectingConfigValue(id, values, codec, property, owner, ConfigValueBuilder(id)))
    }

    override fun button(id: String, onClick: () -> Unit) {
        TODO("Not yet implemented")
    }
}
