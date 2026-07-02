package xyz.bluspring.unitytranslate.translator.instance.index

import java.nio.file.Path

abstract class PackageIndex<T : ModelPackage>(val path: Path, val name: String) {
    val packages = mutableListOf<T>()

    abstract fun loadIndex()
    abstract fun loadIndexOrCache()

    abstract fun getAvailableModelInfo(pkg: T): ModelInfo?
    abstract suspend fun tryDownloadModelInfo(pkg: T): ModelInfo

    open fun isModelAvailable(fromLang: String, toLang: String): Boolean {
        val packages = this.getTranslationPackage(fromLang, toLang)
        if (packages.isEmpty())
            return false

        for (pkg in packages) {
            if (getAvailableModelInfo(pkg) == null)
                return false
        }

        return true
    }

    open fun getAvailableModelInfos(fromLang: String, toLang: String): Map<T, ModelInfo> {
        val packages = this.getTranslationPackage(fromLang, toLang)
        if (packages.isEmpty())
            return mapOf()

        val modelInfos = mutableMapOf<T, ModelInfo>()
        for (pkg in packages) {
            modelInfos[pkg] = getAvailableModelInfo(pkg) ?: continue
        }

        return modelInfos
    }

    open fun getUnavailablePackages(fromLang: String, toLang: String): List<T> {
        val packages = this.getTranslationPackage(fromLang, toLang)
        if (packages.isEmpty())
            return emptyList()

        val unavailable = mutableListOf<T>()
        for (pkg in packages) {
            if (getAvailableModelInfo(pkg) == null)
                unavailable.add(pkg)
        }

        return unavailable
    }

    open suspend fun tryDownloadModelInfos(fromLang: String, toLang: String): Map<T, ModelInfo> {
        val packages = this.getTranslationPackage(fromLang, toLang)
        if (packages.isEmpty())
            return mapOf()

        val modelInfos = mutableMapOf<T, ModelInfo>()
        for (pkg in packages) {
            modelInfos[pkg] = tryDownloadModelInfo(pkg)
        }

        return modelInfos
    }

    open fun getTranslationPackage(fromLang: String, toLang: String): List<T> {
        if (this.packages.isEmpty())
            this.loadIndexOrCache()

        // Check if there's a direct translation
        if (this.packages.any { it.fromCode == fromLang && it.toCode == toLang }) {
            return listOf(this.packages.first { it.fromCode == fromLang && it.toCode == toLang })
        }

        // If not, see if it's possible to do translations via another language
        val fromLanguages = this.packages.filter { it.fromCode == fromLang }
        val toLanguages = this.packages.filter { it.toCode == toLang }

        if (fromLanguages.isEmpty() || toLanguages.isEmpty())
            return emptyList()

        val possibleLanguages = toLanguages.filter { fromLanguages.any { a -> a.toCode == it.fromCode } }

        if (possibleLanguages.isEmpty())
            return emptyList()

        val toLanguage = possibleLanguages.first()
        val fromLanguage = fromLanguages.first { it.toCode == toLanguage.fromCode }

        return listOf(fromLanguage, toLanguage)
    }
}
