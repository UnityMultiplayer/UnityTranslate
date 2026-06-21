package xyz.bluspring.unitytranslate.config.values

import com.mojang.serialization.Codec
import xyz.bluspring.sunset.values.ReflectingConfigValue
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import kotlin.reflect.KMutableProperty

open class ValidatingReflectingListConfigValue<T>(id: String, codec: Codec<T>, property: KMutableProperty<MutableList<T>>, owner: Any? = null, override val validator: ConfigValueBuilder<MutableList<T>>) : ReflectingConfigValue<MutableList<T>>(id, codec.listOf().xmap({ it.toMutableList() }, { it }), property, owner), ValidatingConfigValue<MutableList<T>> {
}
