package xyz.bluspring.unitytranslate.transcriber.whisper

import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.plugin.UnityTranslatePlugin

class WhisperTranscriberPlugin : UnityTranslatePlugin {
    override fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata) {
        api.registerTranscriber("unitytranslate_whisper", WhisperTranscriber) {
            downloadableDropdown("model", WhisperModel.entries, WhisperModel.CODEC, WhisperTranscriber::model)
            integer("max_threads", 1, Runtime.getRuntime().availableProcessors(), 1, WhisperTranscriber::maxWhisperThreads)
            boolean("enable_gpu", WhisperTranscriber::enableGpu)
        }
    }
}
