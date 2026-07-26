package xyz.bluspring.unitytranslate.translator

import kotlinx.coroutines.*
import kotlinx.coroutines.future.asCompletableFuture
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.event.TranscriptEvent
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorManager
import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import xyz.bluspring.unitytranslate.transcriber.DirectTranscriptData
import xyz.bluspring.unitytranslate.transcriber.TranslatedTranscriptData
import xyz.bluspring.unitytranslate.translator.instance.InactiveTranslatorInstance
import xyz.bluspring.unitytranslate.translator.instance.argos.UnityTranslateLibTranslatorInstance
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
    private lateinit var prepareScope: CoroutineScope

    private val tickScope = CoroutineScope(Dispatchers.Default) + CoroutineName("UnityTranslate Translator Manager Tick")

    init {
        this.updateConfig()

        TranscriptEvent.UPDATED.register { holder, data ->
            // make sure we're only handling direct transcript data
            if (data !is DirectTranscriptData)
                return@register

            UnityTranslateApiImpl.transcriptHolders.forEach { (toLang, otherHolder) ->
                this.queue(data.message, holder.language, toLang)
                    .asCompletableFuture()
                    .thenAccept { translated ->
                        otherHolder.update(TranslatedTranscriptData(
                            data.timeCreated,
                            data.sender,
                            data.language,
                            data.message,
                            translated,
                            data.timeUpdated,
                        ))
                    }
            }
        }
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

            if (this::prepareScope.isInitialized) {
                this.prepareScope.cancel("Thread count updated (${this.lastMaxThreads} -> ${Config.maxThreads})")
            }

            val tasks = if (this::pool.isInitialized) {
                this.pool.shutdownNow()
            } else emptyList()

            this.pool = Executors.newWorkStealingPool(Config.maxThreads)
            this.context = pool.asCoroutineDispatcher() + CoroutineName("UnityTranslate Translator Manager")
            this.scope = CoroutineScope(this.context) + CoroutineName("UnityTranslate Translator Manager (Translation Scope)")
            this.prepareScope = CoroutineScope(this.context) + CoroutineName("UnityTranslate Translator Manager (Preparation Scope)")

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
            while (true) {
                tick()
                yield()
                LockSupport.parkNanos("Ticking for UnityTranslate translator", 50.milliseconds.inWholeNanoseconds)
            }
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
                prepareScope.async { instance.prepareTranslationModels(langPair) }.await() // Make sure they're ready first.

                val batchTranslated = instance.batchTranslate(entries.map { it.original }, langPair)
                for ((i, translated) in batchTranslated.withIndex()) {
                    val original = translated
                    var translated = original

                    for ((processor, settings) in UnityTranslateApiImpl.postProcessors) {
                        if ((settings.translatorInputLanguages == null || settings.translatorInputLanguages!!.contains(langPair.from)) && (settings.translatorOutputLanguages == null || settings.translatorOutputLanguages!!.contains(langPair.to)))
                            translated = processor.processFinalTranscript(translated, original, langPair.from, langPair.to)
                    }

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
