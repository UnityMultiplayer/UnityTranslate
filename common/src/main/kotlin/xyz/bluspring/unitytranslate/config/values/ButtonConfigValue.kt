package xyz.bluspring.unitytranslate.config.values

import xyz.bluspring.sunset.values.NonConfigValue
import xyz.bluspring.unitytranslate.api.v2.config.ConfigButtonBuilder

class ButtonConfigValue(id: String, override val validator: ConfigButtonBuilder) : NonConfigValue(id), ValidatingConfigValue<Unit>
