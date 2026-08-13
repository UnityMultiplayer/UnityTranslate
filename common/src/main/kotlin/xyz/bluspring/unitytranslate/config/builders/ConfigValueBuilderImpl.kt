package xyz.bluspring.unitytranslate.config.builders

import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent

open class ConfigValueBuilderImpl<T>(val id: String) : ConfigValueBuilder<T> {
    internal var validator: (T) -> Boolean = { true }
    internal var formatter: ((T) -> TextComponent)? = null

    override fun validator(validator: (T) -> Boolean) {
        this.validator = validator
    }

    override fun formatting(formatter: (T) -> TextComponent) {
        this.formatter = formatter
    }
}
