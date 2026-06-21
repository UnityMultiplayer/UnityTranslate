package xyz.bluspring.unitytranslate.translator

import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorManager
import xyz.bluspring.unitytranslate.library.util.LangPair
import xyz.bluspring.unitytranslate.translator.instance.UnityTranslateLibTranslatorInstance
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

object TranslatorManagerImpl : TranslatorManager {
    object Config {
        var maxThreads: Int = 3.coerceAtMost(Runtime.getRuntime().availableProcessors())
        var batchSize: Int = 15
        var delayBetweenBatches: Int = 500
    }

    private lateinit var context: CoroutineContext
    private lateinit var scope: CoroutineScope

    init {
        this.updateConfig()
    }

    override var instances = mutableListOf<TranslatorInstance>(
        UnityTranslateLibTranslatorInstance,
    )

    override fun getInstanceById(id: String): TranslatorInstance? {
        return UnityTranslateApiImpl.translators[id]
    }

    private val queued = ConcurrentHashMap<LangPair, Queue<Entry>>()
    private var lastBatchTime: Long = 0L

    @JvmRecord
    private data class Entry(
        val original: String,
        val deferred: CompletableDeferred<String>
    )

    private var lastMaxThreads = -1

    private fun updateConfig() {
        if (this.lastMaxThreads != Config.maxThreads) {
            this.scope.cancel("Thread count updated (${this.lastMaxThreads} -> ${Config.maxThreads})")

            this.context = Dispatchers.Default.limitedParallelism(Config.maxThreads) + CoroutineName("UnityTranslate Translator Manager")
            this.scope = CoroutineScope(this.context)

            this.lastMaxThreads = Config.maxThreads
        }
    }

    override fun queue(text: String, langPair: LangPair): Deferred<String> {
        val deferred: CompletableDeferred<String> = CompletableDeferred()
        this.queued.computeIfAbsent(langPair) { ConcurrentLinkedQueue() }
            .add(Entry(text, deferred))

        return deferred
    }

    suspend fun tick() {
        this.updateConfig()

        // just to try to collect enough to even batch translate.
        if (System.currentTimeMillis() - this.lastBatchTime <= Config.delayBetweenBatches)
            return

        var anyTranslationsQueued = false
        for ((langPair, queue) in this.queued) {
            if (queue.isEmpty())
                continue

            val instance = this.instances.firstOrNull {
                it.isAvailable() && it.supportsLanguage(langPair)
            }

            if (instance == null)
                continue

            val entries = ArrayList<Entry>(Config.batchSize)
            while (queue.isNotEmpty()) {
                val entry = queue.poll()!!

                // skip over any entries that may have been superseded.
                if (entry.deferred.isCancelled)
                    continue

                entries.add(entry)

                if (entries.size >= Config.batchSize)
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
