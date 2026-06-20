package xyz.bluspring.unitytranslate.api.v2.config

import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import kotlin.reflect.KMutableProperty

interface ConfigBuilder {
    fun category(id: String, builder: ConfigBuilder.() -> Unit)

    fun validator(validator: () -> Boolean)
    fun <T> value(id: String, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, builder: ConfigValueBuilder<T>.() -> Unit = {})
    fun integer(id: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, step: Int = 1, property: KMutableProperty<Int>, owner: Any? = null, builder: ConfigValueBuilder<Int>.() -> Unit = {})
    fun boolean(id: String, property: KMutableProperty<Boolean>, owner: Any? = null, builder: ConfigValueBuilder<Boolean>.() -> Unit = {})
    fun float(id: String, min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE, step: Float = 0.1f, property: KMutableProperty<Float>, owner: Any? = null, builder: ConfigValueBuilder<Float>.() -> Unit = {})
    fun double(id: String, min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE, step: Double = 0.1, property: KMutableProperty<Double>, owner: Any? = null, builder: ConfigValueBuilder<Double>.() -> Unit = {})
    fun string(id: String, property: KMutableProperty<String>, owner: Any? = null, builder: ConfigValueBuilder<String>.() -> Unit = {})
    fun <T> dropdown(id: String, values: Collection<T>, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, builder: ConfigValueBuilder<T>.() -> Unit = {})
    fun <T : DownloadableEntry> downloadableDropdown(id: String, values: Collection<T>, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, builder: ConfigValueBuilder<T>.() -> Unit = {})
    fun button(id: String, builder: ConfigButtonBuilder.() -> Unit)
    fun separator()
    fun label(id: String, builder: ConfigValueBuilder<Unit>.() -> Unit)
    fun intColor(id: String, property: KMutableProperty<Int>, owner: Any? = null, builder: ConfigValueBuilder<Int>.() -> Unit = {})
}
