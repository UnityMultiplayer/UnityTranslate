package xyz.bluspring.unitytranslate.transcriber

import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.transcriber.TranscriptData.Companion.id
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
            val processed = source.processSamples()
            val holder = UnityTranslateApi.instance.getOrCreateTranscriptHolder(source.language)
            val transcriptData = DirectTranscriptData(source.sessionTimestamp, source.sender, source.language, processed)
            synchronized(holder.transcripts) {
                holder.transcripts.removeIf { it.id == transcriptData.id }
                holder.transcripts.add(transcriptData)
            }

            TranscriptEvent.UPDATED.invoker().onTranscriptUpdated(holder, transcriptData)
        }
    }
}
