package xyz.bluspring.unitytranslate.translator.instance.index

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper
import xyz.bluspring.unitytranslate.library.util.TokenizerType
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.zip.ZipFile
import kotlin.io.path.*
import kotlin.time.Duration.Companion.days

class ArgosPackageIndex(path: Path) : PackageIndex<ArgosPackage>(path, "argos") {
    private var lastIndexTime = 0L

    override suspend fun loadIndex() {
        val url = URI.create(PACKAGE_INDEX_URL).toURL()
        withContext(Dispatchers.IO) {
            url.openStream()
        }.use { loadIndexFromStream(it, true) }

        lastIndexTime = System.currentTimeMillis()
    }

    private fun loadIndexFromStream(stream: InputStream, cache: Boolean = false) {
        val indexData = ArgosPackage.CODEC.listOf().decode(JsonOps.INSTANCE, JsonParser.parseReader(stream.reader())).orThrow.first

        synchronized(this.packages) {
            this.packages.clear()
            this.packages.addAll(indexData)
        }

        if (cache) {
            val cachedFile = path.resolve("index.json")
            if (!cachedFile.exists()) {
                cachedFile.createParentDirectories()
                cachedFile.createFile()
            }

            cachedFile.writeText(ArgosPackage.CODEC.listOf().encodeStart(JsonOps.INSTANCE, this.packages).orThrow.toString(), Charsets.UTF_8, options = arrayOf(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))
        }
    }

    override suspend fun loadIndexOrCache(waitForIndexUpdate: Boolean) {
        val cachedFile = path.resolve("index.json")

        if (lastIndexTime == 0L && cachedFile.exists()) {
            lastIndexTime = cachedFile.getLastModifiedTime().toMillis()
        }

        if (System.currentTimeMillis() - lastIndexTime >= 1.days.inWholeMilliseconds) {
            try {
                UnityTranslate.logger.debug("Cache outdated! Updating Argos index.")
                if (waitForIndexUpdate) {
                    loadIndex()
                } else {
                    coroutineScope {
                        launch {
                            loadIndex()
                            yield()
                        }
                    }
                }
            } catch (e: Exception) {
                if (!cachedFile.exists())
                    UnityTranslate.logger.debug("Failed to update Argos index, and no cached index could be found!", e)
            }
        }

        // Try to load the cached data in the meantime.
        if (cachedFile.exists()) {
            cachedFile.inputStream(options = arrayOf(StandardOpenOption.READ)).use { loadIndexFromStream(it) }
        }
    }

    override fun getAvailableModelInfo(pkg: ArgosPackage): ModelInfo? {
        val pkgDir = path.resolve("${pkg.code}_${pkg.packageVersion}")

        if (pkgDir.exists())
            return createModelInfo(pkg, pkgDir)

        return null
    }

    override suspend fun tryDownloadModelInfo(pkg: ArgosPackage): ModelInfo {
        val pkgDir = path.resolve("${pkg.code}_${pkg.packageVersion}")

        if (pkgDir.exists()) {
            return createModelInfo(pkg, pkgDir)
        }

        val exception = RuntimeException("Failed to download Argos models for ${pkg.code} v${pkg.packageVersion}!")

        for (link in pkg.links) {
            try {
                val url = URI.create(link).toURL()
                val zipPath = path.resolve("${pkg.code}_${pkg.packageVersion}.argosmodel")

                val info = DownloadHelper.queue(url, zipPath)
                info.deferred.await()

                val zipFile = withContext(Dispatchers.IO) {
                    ZipFile(zipPath.toFile())
                }

                for (entry in zipFile.entries()) {
                    if (entry.name.endsWith("/"))
                        continue

                    val file = pkgDir.resolve(entry.name.replaceBefore("/", "").replaceFirst("/", ""))
                    if (!file.parent.exists())
                        file.createParentDirectories()

                    withContext(Dispatchers.IO) {
                        zipFile.getInputStream(entry)
                    }.use {
                        file.outputStream(options = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)).use { f ->
                            it.copyTo(f)
                        }
                    }
                }

                zipPath.deleteIfExists()

                return createModelInfo(pkg, pkgDir)
            } catch (e: Exception) {
                exception.addSuppressed(RuntimeException("Failed to download from URL $link", e))
            }
        }

        throw exception
    }

    private fun createModelInfo(pkg: ArgosPackage, pkgDir: Path): ModelInfo {
        val bpeModel = pkgDir.resolve("bpe.model")
        val spModel = pkgDir.resolve("sentencepiece.model")

        val tokenizerType = if (spModel.exists())
            TokenizerType.SENTENCEPIECE
        else if (bpeModel.exists())
            TokenizerType.BPE
        else throw IllegalStateException("Could not determine tokenizer type for package ${pkg.from} -> ${pkg.to}")

        return ModelInfo(
            pkg.code, pkg.langPair,
            pkgDir.resolve("model"),
            tokenizerType,
            when (tokenizerType) {
                TokenizerType.SENTENCEPIECE -> spModel
                TokenizerType.BPE -> bpeModel
            }
        )
    }

    companion object {
        const val PACKAGE_INDEX_ROOT_URL = "https://raw.githubusercontent.com/argosopentech/argospm-index/main"
        const val PACKAGE_INDEX_URL = "$PACKAGE_INDEX_ROOT_URL/index.json"
    }
}
