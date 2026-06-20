package xyz.bluspring.unitytranslate.api.v2.config

interface ConfigButtonBuilder : ConfigValueBuilder<Unit> {
    fun onClick(event: () -> Unit)
}
