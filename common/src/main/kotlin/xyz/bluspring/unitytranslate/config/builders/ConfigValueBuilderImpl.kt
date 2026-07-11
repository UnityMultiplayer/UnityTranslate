package xyz.bluspring.unitytranslate.config.builders

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.config.ConfigValueBuilder

open class ConfigValueBuilderImpl<T>(val id: String) : ConfigValueBuilder<T> {
    internal var validator: (T) -> Boolean = { true }
    internal var formatter: ((T) -> Component)? = null

    override fun validator(validator: (T) -> Boolean) {
        this.validator = validator
    }

    override fun formatting(formatter: (T) -> Component) {
        this.formatter = formatter
    }
}
