package xyz.bluspring.unitytranslate.api.v2.plugin

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi

/**
 * A plugin that can be registered into UnityTranslate for extending onto its functionality.
 *
 * A `unitytranslate.plugin.json` should be provided in the root of the plugin JAR for UnityTranslate
 * to discover it, and this interface must be implemented in the class being pointed at by the "main" value
 * of the plugin metadata.
 *
 * The plugin API is not very well-designed for Java usage, and is better to be written in Kotlin instead.
 */
interface UnityTranslatePlugin {
    fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata)
}
