package xyz.bluspring.unitytranslate.translator.instance.index

import xyz.bluspring.unitytranslate.api.v2.translator.TranslatorInstance
import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import java.nio.file.Path

abstract class PackageIndex<T : ModelPackage>(val path: Path, val name: String) {
    val packages = mutableListOf<T>()

    abstract suspend fun loadIndex()
    abstract suspend fun loadIndexOrCache(waitForIndexUpdate: Boolean = false)

    abstract fun getAvailableModelInfo(pkg: T): ModelInfo?
    abstract suspend fun tryDownloadModelInfo(pkg: T): ModelInfo

    open suspend fun isModelAvailable(langPair: LangPair): Boolean {
        val packages = this.getTranslationPackage(langPair)
        if (packages.isEmpty())
            return false

        for (pkg in packages) {
            if (getAvailableModelInfo(pkg) == null)
                return false
        }

        return true
    }

    open suspend fun getAvailableModelInfos(langPair: LangPair): Map<T, ModelInfo> {
        val packages = this.getTranslationPackage(langPair)
        if (packages.isEmpty())
            return mapOf()

        val modelInfos = mutableMapOf<T, ModelInfo>()
        for (pkg in packages) {
            modelInfos[pkg] = getAvailableModelInfo(pkg) ?: continue
        }

        return modelInfos
    }

    open suspend fun getUnavailablePackages(langPair: LangPair): List<T> {
        val packages = this.getTranslationPackage(langPair)
        if (packages.isEmpty())
            return emptyList()

        val unavailable = mutableListOf<T>()
        for (pkg in packages) {
            if (getAvailableModelInfo(pkg) == null)
                unavailable.add(pkg)
        }

        return unavailable
    }

    open suspend fun tryDownloadModelInfos(langPair: LangPair): Map<T, ModelInfo> {
        val packages = this.getTranslationPackage(langPair)
        if (packages.isEmpty())
            return mapOf()

        val modelInfos = mutableMapOf<T, ModelInfo>()
        for (pkg in packages) {
            modelInfos[pkg] = tryDownloadModelInfo(pkg)
        }

        return modelInfos
    }

    open suspend fun getTranslationPackage(expected: LangPair): List<T> {
        if (this.packages.isEmpty())
            this.loadIndexOrCache()

        val translationPath = TranslatorInstance.getAvailableTranslationPath(expected, this.packages.map { it.langPair })
        return translationPath.map { langPair -> this.getDirectTranslationPackage(langPair)!! }
    }

    open suspend fun getDirectTranslationPackage(expected: LangPair): T? {
        if (this.packages.isEmpty())
            this.loadIndexOrCache()

        return this.packages.firstOrNull { it.langPair == expected }
    }
}
