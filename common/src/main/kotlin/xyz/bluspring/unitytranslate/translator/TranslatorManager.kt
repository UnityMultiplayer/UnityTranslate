package xyz.bluspring.unitytranslate.translator

import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.library.util.LangPair
import xyz.bluspring.unitytranslate.translator.instance.TranslatorInstance
import xyz.bluspring.unitytranslate.translator.instance.UnityTranslateLibTranslatorInstance
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class TranslatorManager(
    val maxThreads: Int = 3.coerceAtMost(Runtime.getRuntime().availableProcessors()),
    val batchSize: Int = 15,
    val delayBetweenBatches: Int = 500,
) {
    private val context = Dispatchers.Default.limitedParallelism(maxThreads) + CoroutineName("UnityTranslate Translator Manager")
    private val scope = CoroutineScope(context)

    val instances = listOf<TranslatorInstance>(
        UnityTranslateLibTranslatorInstance
    )

    private val queued = ConcurrentHashMap<LangPair, Queue<Entry>>()
    private var lastBatchTime: Long = 0L

    @JvmRecord
    private data class Entry(
        val original: String,
        val deferred: CompletableDeferred<String>
    )

    fun queue(text: String, fromLang: String, toLang: String): Deferred<String> {
        val deferred: CompletableDeferred<String> = CompletableDeferred()
        this.queued.computeIfAbsent(LangPair(fromLang, toLang)) { ConcurrentLinkedQueue() }
            .add(Entry(text, deferred))

        return deferred
    }

    fun tick() {
        // just to try to collect enough to even batch translate.
        if (System.currentTimeMillis() - this.lastBatchTime <= this.delayBetweenBatches)
            return

        var anyTranslationsQueued = false
        for ((langPair, queue) in this.queued) {
            if (queue.isEmpty())
                continue

            val instance = this.instances.firstOrNull {
                it.isAvailable && it.supportsLanguage(langPair)
            }

            if (instance == null)
                continue

            val entries = ArrayList<Entry>(this.batchSize)
            while (queue.isNotEmpty()) {
                val entry = queue.poll()!!

                // skip over any entries that may have been superseded.
                if (entry.deferred.isCancelled)
                    continue

                entries.add(entry)

                if (entries.size >= this.batchSize)
                    break
            }

            this.scope.launch(this.context) {
                val batchTranslated = instance.batchTranslate(entries.map { it.original }, langPair)
                for ((i, translated) in batchTranslated.withIndex()) {
                    entries[i].deferred.complete(translated)
                }
            }

            anyTranslationsQueued = true
        }

        if (anyTranslationsQueued) {
            this.lastBatchTime = System.currentTimeMillis()
        }
    }
}
