package xyz.bluspring.unitytranslate.config.values

import com.mojang.serialization.Codec
import xyz.bluspring.sunset.values.ReflectingConfigValue
import kotlin.reflect.KMutableProperty

class HiddenReflectingConfigValue<T>(id: String, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null) : ReflectingConfigValue<T>(id, codec, property, owner), HiddenConfigValue {
}
