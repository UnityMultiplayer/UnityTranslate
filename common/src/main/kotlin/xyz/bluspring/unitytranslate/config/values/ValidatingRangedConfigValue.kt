package xyz.bluspring.unitytranslate.config.values

import com.mojang.serialization.Codec
import xyz.bluspring.sunset.values.RangedConfigValue
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import kotlin.reflect.KMutableProperty

class ValidatingRangedConfigValue<T : Number>(id: String, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, min: T, max: T, step: T, override val validator: ConfigValueBuilder<T>) : RangedConfigValue<T>(id, codec, property, owner, min, max, step), ValidatingConfigValue<T> {
}
