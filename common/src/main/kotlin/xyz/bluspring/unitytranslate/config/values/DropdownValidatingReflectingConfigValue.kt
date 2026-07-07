package xyz.bluspring.unitytranslate.config.values

import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import xyz.bluspring.unitytranslate.api.v2.config.NameProvidingEntry
import kotlin.reflect.KMutableProperty

open class DropdownValidatingReflectingConfigValue<T : NameProvidingEntry>(id: String, val values: Collection<T>, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, validator: ConfigValueBuilder<T>) : ValidatingReflectingConfigValue<T>(id, codec, property, owner, validator) {
}
