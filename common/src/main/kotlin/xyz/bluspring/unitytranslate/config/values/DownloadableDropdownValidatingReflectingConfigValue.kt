package xyz.bluspring.unitytranslate.config.values

import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import xyz.bluspring.unitytranslate.api.v2.config.NameProvidingEntry
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import kotlin.reflect.KMutableProperty

open class DownloadableDropdownValidatingReflectingConfigValue<T>(id: String, values: Collection<T>, codec: Codec<T>, property: KMutableProperty<T>, owner: Any? = null, validator: ConfigValueBuilder<T>) : DropdownValidatingReflectingConfigValue<T>(id, values, codec, property, owner, validator) where T : NameProvidingEntry, T : DownloadableEntry {
}
