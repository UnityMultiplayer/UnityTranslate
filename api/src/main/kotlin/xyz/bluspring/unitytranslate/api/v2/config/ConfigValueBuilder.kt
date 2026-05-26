package xyz.bluspring.unitytranslate.api.v2.config

import net.minecraft.network.chat.Component
import org.jetbrains.annotations.ApiStatus

class ConfigValueBuilder<T> @ApiStatus.Internal constructor(val id: String) {
    /**
     * If this returns true, the value is valid to be used and stored.
     */
    var validator: (T) -> Boolean = { true }
    var formatter: (T) -> Component = { Component.literal(it.toString()) }
}
