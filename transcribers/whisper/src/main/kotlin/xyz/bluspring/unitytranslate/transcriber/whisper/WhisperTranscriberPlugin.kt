package xyz.bluspring.unitytranslate.transcriber.whisper

import io.github.ggerganov.whispercpp.WhisperCppJnaLibrary
import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.plugin.PluginMetadata
import xyz.bluspring.unitytranslate.api.v2.plugin.UnityTranslatePlugin
import java.util.concurrent.Executors

class WhisperTranscriberPlugin : UnityTranslatePlugin {
    override fun onLoadPlugin(api: UnityTranslateApi, metadata: PluginMetadata) {
        runBlocking {
            launch(initThread, start = CoroutineStart.UNDISPATCHED) {
                WhisperCppJnaLibrary.instance // just call it.
            }
        }

        api.registerTranscriber("unitytranslate_whisper", WhisperTranscriber) {
            downloadableDropdown("model", WhisperModel.entries, WhisperModel.CODEC, WhisperTranscriber::model)
            integer("max_threads", 1, Runtime.getRuntime().availableProcessors(), 1, WhisperTranscriber::maxWhisperThreads)
            boolean("enable_gpu", WhisperTranscriber::enableGpu)
        }
    }

    companion object {
        // okay.. yeah.. cool... yep... that's normal...
        val initThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher() + CoroutineName("Whisper Init Thread")
    }
}
