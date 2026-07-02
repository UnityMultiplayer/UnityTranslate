package xyz.bluspring.unitytranslate.translator.instance

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import xyz.bluspring.unitytranslate.library.UnityTranslateLib
import xyz.bluspring.unitytranslate.library.UnityTranslateLibInstance
import java.util.*

object UnityTranslateLibTranslatorInstance : TranslatorInstance() {
    var enableGpu = false
    private var lastEnabledGpu = false

    // Argos' index uses unofficial codes, so we need to remap them.
    // This is based on https://github.com/LibreTranslate/LibreTranslate/blob/main/libretranslate/language.py#L9
    val unofficialToOfficialAliases = mapOf(
        "pt-BR" to "pb",
        "zh-Hans" to "zh",
        "zh-Hant" to "zt",
    )

    val library = UnityTranslateLib(UnityTranslateApi.instance.storagePath.resolve("library"))
    val instances: MutableMap<LangPair, UnityTranslateLibInstance> = Collections.synchronizedMap(mutableMapOf<LangPair, UnityTranslateLibInstance>())
    private val instanceLocks: MutableMap<LangPair, Mutex> = Collections.synchronizedMap(mutableMapOf<LangPair, Mutex>())

    private val gpuEnabledLock = Mutex()

    override suspend fun isAvailable(): Boolean = UnityTranslateLib.isAvailable()

    override suspend fun supportsLanguage(langPair: LangPair): Boolean {
        return true
    }

    private fun acquireLock(langPair: LangPair): Mutex {
        synchronized(this.instanceLocks) {
            return this.instanceLocks.computeIfAbsent(langPair) { Mutex() }
        }
    }

    override suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String> {
        // Reset the instances so we actually switch to using the GPU.
        this.gpuEnabledLock.withLock {
            if (this.lastEnabledGpu != this.enableGpu) {
                for ((langPair, instance) in this.instances) {
                    val lock = this.acquireLock(langPair)
                    lock.withLock {
                        instance.free()
                    }
                }

                this.instances.clear()
                this.lastEnabledGpu = this.enableGpu
            }
        }

        val lock = this.acquireLock(langPair)

        lock.withLock {
            val instance = this.instances[langPair]!!
            return instance.batchTranslate(text)
        }
    }
}
