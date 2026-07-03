package xyz.bluspring.unitytranslate.translator

import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorManager
import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import xyz.bluspring.unitytranslate.translator.instance.InactiveTranslatorInstance
import xyz.bluspring.unitytranslate.translator.instance.UnityTranslateLibTranslatorInstance
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

object TranslatorManagerImpl : TranslatorManager {
    object Config {
        var maxThreads: Int = 3.coerceAtMost(Runtime.getRuntime().availableProcessors())
        var batchSize: Int = 15
        var delayBetweenBatches: Int = 500
    }

    private lateinit var pool: ExecutorService
    private lateinit var context: CoroutineContext
    private lateinit var scope: CoroutineScope

    private val tickScope = CoroutineScope(Dispatchers.Default) + CoroutineName("UnityTranslate Translator Manager Tick")

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
            if (this::scope.isInitialized) {
                this.scope.cancel("Thread count updated (${this.lastMaxThreads} -> ${Config.maxThreads})")
            }

            val tasks = if (this::pool.isInitialized) {
                this.pool.shutdownNow()
            } else emptyList()

            this.pool = Executors.newFixedThreadPool(Config.maxThreads)
            this.context = pool.asCoroutineDispatcher() + CoroutineName("UnityTranslate Translator Manager")
            this.scope = CoroutineScope(this.context) + CoroutineName("UnityTranslate Translator Manager")

            for (task in tasks) {
                this.pool.submit(task)
            }

            this.lastMaxThreads = Config.maxThreads
        }
    }

    override fun queue(text: String, langPair: LangPair): Deferred<String> {
        val deferred: CompletableDeferred<String> = CompletableDeferred()
        this.queued.computeIfAbsent(langPair) { ConcurrentLinkedQueue() }
            .add(Entry(text, deferred))

        return deferred
    }

    fun startTicking() {
        this.tickScope.launch {
            tick()
            yield()
            LockSupport.parkNanos("Ticking for UnityTranslate translator", 50.milliseconds.inWholeNanoseconds)
        }
    }

    private suspend fun tick() {
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
            } ?: InactiveTranslatorInstance

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
                instance.prepareTranslationModels(langPair) // Make sure they're ready first.
                val batchTranslated = instance.batchTranslate(entries.map { it.original }, langPair)
                for ((i, translated) in batchTranslated.withIndex()) {
                    entries[i].deferred.complete(translated)
                }
                yield()
            }

            anyTranslationsQueued = true
        }

        if (anyTranslationsQueued) {
            this.lastBatchTime = System.currentTimeMillis()
        }
    }
}
