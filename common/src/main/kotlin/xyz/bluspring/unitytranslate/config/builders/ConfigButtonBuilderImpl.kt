package xyz.bluspring.unitytranslate.config.builders

import xyz.bluspring.unitytranslate.api.v2.config.ConfigButtonBuilder

class ConfigButtonBuilderImpl(id: String) : ConfigValueBuilderImpl<Unit>(id), ConfigButtonBuilder {
    internal var onClick: () -> Unit = {}

    override fun onClick(event: () -> Unit) {
        this.onClick = event
    }
}
