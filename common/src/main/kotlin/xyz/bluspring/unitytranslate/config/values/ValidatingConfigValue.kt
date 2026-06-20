package xyz.bluspring.unitytranslate.config.values

import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder

interface ValidatingConfigValue<T> {
    val validator: ConfigValueBuilder<T>
}
