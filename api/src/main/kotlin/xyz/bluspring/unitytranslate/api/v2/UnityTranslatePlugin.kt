package xyz.bluspring.unitytranslate.api.v2

import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import java.util.*

/**
 * A plugin that can be registered into UnityTranslate for extending onto its functionality.
 *
 * These plugin interfaces must be implemented and registered as a service (see: [ServiceLoader]),
 * and a `unitytranslate.plugin.json` should be provided in the root of the plugin JAR for UnityTranslate
 * to discover it.
 */
fun interface UnityTranslatePlugin {
    fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata)
}
