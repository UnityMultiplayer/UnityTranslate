package xyz.bluspring.unitytranslate.translator

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import xyz.bluspring.unitytranslate.Language
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.library.UnityTranslateLib
import xyz.bluspring.unitytranslate.library.util.collect
import xyz.bluspring.unitytranslate.library.util.concurrent

class UnityTranslateLibInstance private constructor(): LibreTranslateInstance("", 150) {
    override val supportedLanguages: Multimap<Language, Language>
        get() {
            if (cachedSupportedLanguages.isEmpty) {
                for (index in library.packageIndex.indexList) {
                    for (pkg in index.packages) {
                        val fromLang = Language.findLibreLang(pkg.fromCode)
                        val toLang = Language.findLibreLang(pkg.toCode)

                        cachedSupportedLanguages.put(fromLang, toLang)
                    }
                }
            }

            return cachedSupportedLanguages
        }

    override suspend fun translate(from: String, to: String, request: String): String {
        return library.getTranslator(from, to, TranslatorManager.checkSupportsCuda()).batchTranslate(listOf(request)).getOrNull(0) ?: request
    }

    override suspend fun batchTranslate(from: String, to: String, request: List<String>): List<String> {
        return library.getTranslator(from, to, TranslatorManager.checkSupportsCuda()).batchTranslate(request)
    }

    companion object {
        private val cachedSupportedLanguages = HashMultimap.create<Language, Language>()
        lateinit var library: UnityTranslateLib
        lateinit var instance: UnityTranslateLibInstance

        var isLibraryLoaded: Boolean = false
            private set

        init {
            try {
                library = UnityTranslateLib(UnityTranslate.instance.proxy.gameDir.resolve(".unitytranslate"))
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        fun tryLoad() {
            try {
                library.load()

                if (FabricLoader.getInstance().isDevelopmentEnvironment || FabricLoader.getInstance().environmentType == EnvType.SERVER) {
                    runBlocking(Dispatchers.IO) {
                        for (index in library.packageIndex.indexList) {
                            index.packages.chunked(10).asFlow().collect {
                                it.asFlow().concurrent().collect {
                                    UnityTranslate.logger.info("Downloading translation model ${it.fromCode}-${it.toCode}")
                                    index.getOrDownloadModelInfos(it.fromCode, it.toCode)
                                    UnityTranslate.logger.info("Downloaded translation model ${it.fromCode}-${it.toCode}")
                                }
                            }
                        }
                    }
                }

                instance = UnityTranslateLibInstance()
                isLibraryLoaded = true
            } catch (e: Throwable) {
                UnityTranslate.logger.error("Failed to load UnityTranslateLib!")
                e.printStackTrace()
            }
        }
    }
}
