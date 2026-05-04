package xyz.bluspring.unitytranslate.transcriber.whisper

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.UnityTranslatePlugin
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata

class WhisperTranscriberPlugin : UnityTranslatePlugin {
    override fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata) {
        api.registerTranscriber("unitytranslate_whisper", WhisperTranscriber())
    }
}