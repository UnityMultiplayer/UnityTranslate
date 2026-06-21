package xyz.bluspring.unitytranslate.transcriber.google

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.plugin.UnityTranslatePlugin
import xyz.bluspring.unitytranslate.transcriber.google.cloud.GoogleCloudTranscriber
import xyz.bluspring.unitytranslate.transcriber.google.internal.GoogleInternalTranscriber

class GoogleTranscriberPlugin : UnityTranslatePlugin {
    override fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata) {
        api.registerTranscriber("google_internal", GoogleInternalTranscriber) {
        }

        api.registerTranscriber("google_cloud", GoogleCloudTranscriber) {
            string("api_key", GoogleCloudTranscriber::apiKey)
        }
    }
}
