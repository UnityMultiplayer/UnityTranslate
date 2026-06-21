package xyz.bluspring.unitytranslate.config.values

import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import kotlin.reflect.KMutableProperty

class IntColorConfigValue(id: String, property: KMutableProperty<Int>, owner: Any? = null, builder: ConfigValueBuilder<Int>) : ValidatingReflectingConfigValue<Int>(id, Codec.INT, property, owner, builder) {
}
