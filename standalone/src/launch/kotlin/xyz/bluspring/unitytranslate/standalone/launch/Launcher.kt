package xyz.bluspring.unitytranslate.standalone.launch

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper
import xyz.bluspring.unitytranslate.api.v2.download.DownloadInfo
import xyz.bluspring.unitytranslate.standalone.Metadata
import xyz.bluspring.unitytranslate.standalone.StandaloneConstants
import xyz.bluspring.unitytranslate.standalone.UnityTranslateStandalone
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.net.URI
import java.nio.file.StandardOpenOption
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JOptionPane
import javax.swing.WindowConstants
import kotlin.io.path.*
import kotlin.system.exitProcess

val librariesPath = Path("libraries")
val metaPath = Path("metadata")

val metadata = Metadata.get()

private fun displayError(message: String) {
    Toolkit.getDefaultToolkit().beep()
    val optionPane = JOptionPane(message, JOptionPane.ERROR_MESSAGE)
    val dialog = optionPane.createDialog("UnityTranslate")

    dialog.setIconImage(ImageIO.read(UnityTranslateStandalone.ICON))
    dialog.isVisible = true
    dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    exitProcess(1)
}

fun main() {
    UnityTranslate.logger.info("Loading UnityTranslate v${metadata.version}... (build hash: ${metadata.buildHash})")

    if (GraphicsEnvironment.isHeadless()) {
        throw IllegalStateException("You're running UnityTranslate in a headless environment, which is not supported!")
    }

    try {
        runBlocking {
            tryDownloadLibraries()
        }
    } catch (e: Throwable) {
        UnityTranslate.logger.error("An error occurred whilst launching UnityTranslate!", e)
        displayError("An error occurred whilst launching UnityTranslate!\n\nError: ${e::class.java.name}: ${e.message ?: e.localizedMessage}")
    }
}

suspend fun tryDownloadLibraries() {
    val mcVersion = metadata.minecraftVersion
    val mcMetaPath = metaPath / "${mcVersion}.json"
    if (!mcMetaPath.exists()) {
        UnityTranslate.logger.info("No metadata found for ${mcVersion}, downloading...")

        val manifestUrl = URI.create(StandaloneConstants.MC_VERSION_MANIFEST).toURL()
        val json = withContext(Dispatchers.IO) {
            manifestUrl.openStream().use { JsonParser.parseReader(it.reader()) }
        }.asJsonObject

        val versionsArray = json.getAsJsonArray("versions")
        val versionData = versionsArray.firstOrNull { it.asJsonObject.get("id").asString == mcVersion }?.asJsonObject

        if (versionData == null) {
            throw IllegalStateException("Failed to find metadata for Minecraft version ${mcVersion}!")
        }

        mcMetaPath.createParentDirectories()

        val metadataUrl = URI.create(versionData.get("url").asString).toURL()
        withContext(Dispatchers.IO) {
            metadataUrl.openStream().use {
                mcMetaPath.writeBytes(it.readAllBytes(), options = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))
            }
        }
    }

    val metadata = withContext(Dispatchers.IO) {
        mcMetaPath.reader().use {
            JsonParser.parseReader(it)
        }
    }.asJsonObject
    val downloadInfos = mutableListOf<DownloadInfo>()

    coroutineScope {
        val clientJarInfo = ArtifactInfo.CODEC.decode(JsonOps.INSTANCE, metadata.getAsJsonObject("downloads").getAsJsonObject("client"))
            .orThrow.first
        val clientJarDownload = DownloadHelper.queue(clientJarInfo.url.toURL(), librariesPath / "com/mojang/minecraft/${mcVersion}/minecraft-$mcVersion.jar", sha1 = clientJarInfo.sha1)
        downloadInfos.add(clientJarDownload)

        libraries@for (element in metadata.getAsJsonArray("libraries")) {
            val libMeta = element.asJsonObject

            // TODO: actually handle rules
//            if (libMeta.has("rules")) {
//                for (ruleJson in libMeta.getAsJsonArray("rules")) {
//                    val rule = ruleJson.asJsonObject
//                    if (rule.get("action").asString == "allow") {
//
//                    }
//                }
//            }

            val downloadData = ArtifactInfo.CODEC.decode(JsonOps.INSTANCE, libMeta.getAsJsonObject("downloads").getAsJsonObject("artifact"))
                .orThrow.first

            val libraryDownload = DownloadHelper.queue(downloadData.url.toURL(), librariesPath / downloadData.path.orElseThrow(), sha1 = downloadData.sha1)
            downloadInfos.add(libraryDownload)
        }

        downloadInfos.map { it.deferred }.awaitAll()
    }
}

@JvmRecord
data class ArtifactInfo(
    val url: URI,
    val size: Long,
    val sha1: String,
    val path: Optional<String>
) {
    companion object {
        val CODEC: Codec<ArtifactInfo> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("url")
                    .xmap(URI::create, URI::toString)
                    .forGetter(ArtifactInfo::url),
                Codec.LONG.fieldOf("size")
                    .forGetter(ArtifactInfo::size),
                Codec.STRING.fieldOf("sha1")
                    .forGetter(ArtifactInfo::sha1),
                Codec.STRING.optionalFieldOf("path")
                    .forGetter(ArtifactInfo::path)
            )
                .apply(instance, ::ArtifactInfo)
        }
    }
}
