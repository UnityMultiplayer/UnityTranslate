package xyz.bluspring.unitytranslate.transcriber

import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.client.config.ClientConfig

class TranscriberManager {
    private var lastTranscriptionTime = 0L
    private var isTranscribing = false

    private val scope = CoroutineScope(Dispatchers.Default) + CoroutineName("UnityTranslate Transcriber Manager")

    fun tick() {
        if (!this.isTranscribing && System.currentTimeMillis() - this.lastTranscriptionTime >= ClientConfig.transcriptionInterval) {
            this.isTranscribing = true
            this.scope.launch {
                try {
                    processTranscriptions()
                } finally {
                    isTranscribing = false
                }
            }

            this.lastTranscriptionTime = System.currentTimeMillis()
        }
    }

    private suspend fun processTranscriptions() {
        val sources = UnityTranslateApiImpl.transcriberSources.values.toList()

        for (source in sources) {
            if (!source.isReadyToProcess)
                continue

            val processed = source.processSamples()
            if (processed.isNotBlank()) {
                val holder = UnityTranslateApi.instance.getOrCreateTranscriptHolder(source.language)
                val transcriptData = DirectTranscriptData(source.sessionTimestamp, source.sender, source.language, processed, System.currentTimeMillis())

                holder.update(transcriptData)
                UnityTranslate.logger.info(processed)
            }
        }
    }
}
