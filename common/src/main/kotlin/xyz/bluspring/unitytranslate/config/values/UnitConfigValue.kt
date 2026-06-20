package xyz.bluspring.unitytranslate.config.values

import xyz.bluspring.sunset.values.NonConfigValue
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder

open class UnitConfigValue(id: String, override val validator: ConfigValueBuilder<Unit>) : NonConfigValue(id), ValidatingConfigValue<Unit>
