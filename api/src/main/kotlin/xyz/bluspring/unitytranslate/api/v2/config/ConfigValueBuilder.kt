package xyz.bluspring.unitytranslate.api.v2.config

import net.minecraft.network.chat.Component

interface ConfigValueBuilder<T> {
    /**
     * If this returns true, the value is valid to be used and stored.
     */
    fun validator(validator: (T) -> Boolean)
    fun formatting(formatter: (T) -> Component)
}
