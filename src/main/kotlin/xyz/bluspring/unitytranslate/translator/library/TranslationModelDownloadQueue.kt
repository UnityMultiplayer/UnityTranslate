package xyz.bluspring.unitytranslate.translator.library

import com.google.common.collect.Queues
import kotlinx.coroutines.runBlocking
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.library.UnityTranslateLib
import java.util.*

object TranslationModelDownloadQueue {
    val library: UnityTranslateLib?
        get() {
            return if (UnityTranslateLibInstance.isLibraryLoaded)
                UnityTranslateLibInstance.library
            else null
        }

    private val alreadyDownloaded = Collections.synchronizedSet(mutableSetOf<Pair<Language, Language>>())

    private val queue = Queues.newSynchronousQueue<Pair<Language, Language>>()
    @Volatile
    private var downloadThread: DownloadThread? = null

    fun queueDownload(from: Language, to: Language) {
        if (library == null) {
            throw IllegalStateException("UnityTranslateLib was not initialized!")
        }

        val langPair = from to to

        if (this.alreadyDownloaded.contains(langPair))
            return

        if (this.queue.contains(langPair))
            return

        this.queue.add(langPair)
        this.tryStartDownloadThread()
    }

    private fun tryStartDownloadThread() {
        if (this.downloadThread == null) {
            this.downloadThread = DownloadThread()
            this.downloadThread!!.start()
        }
    }

    private class DownloadThread : Thread("UnityTranslate Translation Model Download Thread") {
        override fun run() {
            while (queue.isNotEmpty()) {
                val langPair = queue.poll() ?: break
                val (from, to) = langPair
                val library = library!!

                runBlocking {
                    for (index in library.packageIndex.indexList) {
                        try {
                            val infos = index.getOrDownloadModelInfos(from.code, to.code)
                            UnityTranslate.logger.info("Successfully downloaded translation models ${infos.keys.joinToString(", ") { "${it.fromCode} -> ${it.toCode}" }} from package index ${index.name}")
                        } catch (e: Throwable) {
                            UnityTranslate.logger.error("Failed to download translation models from package index ${index.name} for languages ${from.code} -> ${to.code}", e)
                        }
                    }
                }

                alreadyDownloaded.add(langPair)
            }

            downloadThread = null
        }
    }
}