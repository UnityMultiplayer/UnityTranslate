package xyz.bluspring.unitytranslate.api.v2.plugin

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi

/**
 * A plugin that can be registered into UnityTranslate for extending onto its functionality.
 *
 * These plugin interfaces must be implemented and registered as a service (see: [java.util.ServiceLoader]),
 * and a `unitytranslate.plugin.json` should be provided in the root of the plugin JAR for UnityTranslate
 * to discover it.
 *
 * The plugin API is not very well-designed for Java usage, and is better to be written in Kotlin instead.
 */
interface UnityTranslatePlugin {
    fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata)
}
