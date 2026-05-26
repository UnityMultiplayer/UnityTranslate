package xyz.bluspring.unitytranslate.config

import com.mojang.serialization.Codec
import xyz.bluspring.sunset.values.ReflectingConfigValue
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import kotlin.reflect.KMutableProperty

open class ValidatingReflectingConfigValue<T>(id: String, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, val validator: ConfigValueBuilder<T>) : ReflectingConfigValue<T>(id, codec, property, owner) {
}
