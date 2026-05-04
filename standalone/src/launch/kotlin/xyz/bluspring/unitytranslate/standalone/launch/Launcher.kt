package xyz.bluspring.unitytranslate.standalone.launch

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.*
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper.bytesToNearestLarge
import xyz.bluspring.unitytranslate.api.v2.download.DownloadInfo
import xyz.bluspring.unitytranslate.standalone.HandledException
import xyz.bluspring.unitytranslate.standalone.Metadata
import xyz.bluspring.unitytranslate.standalone.StandaloneConstants
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.io.path.*
import kotlin.system.exitProcess

val librariesPath = Path("libraries")
val metaPath = Path("metadata")

val metadata = Metadata.get()

private fun displayError(message: String) {
    Toolkit.getDefaultToolkit().beep()
    val optionPane = JOptionPane(message, JOptionPane.ERROR_MESSAGE)
    val dialog = optionPane.createDialog("UnityTranslate")

    dialog.setIconImage(ImageIO.read(StandaloneConstants.ICON))
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
            // Download all libraries and everything will be fine :D
            val libraries = tryDownloadLibraries()

            // See, told you it'd be fine! Time to load all the libraries and launch.
            val urls = listOf(
                UnityTranslateApi::class.java.protectionDomain.codeSource.location,
                UnityTranslate::class.java.protectionDomain.codeSource.location,
                StandaloneConstants::class.java.protectionDomain.codeSource.location,
            )
            val classLoader = URLClassLoader((libraries.map { it.toUri().toURL() } + urls).toTypedArray(), FilteredClassLoader)

            Thread.currentThread().contextClassLoader = classLoader

            // We're gonna use reflection to launch, so we are able to use the correct class loader.
            val standaloneClass = classLoader.loadClass("xyz.bluspring.unitytranslate.standalone.UnityTranslateStandalone")
            standaloneClass.getDeclaredMethod("init").invoke(null)
        }
    } catch (e: HandledException) {
        UnityTranslate.logger.error("UnityTranslate has crashed!", e)
        exitProcess(1)
    } catch (e: Throwable) {
        // it wasn't fine D:
        UnityTranslate.logger.error("An error occurred whilst launching UnityTranslate!", e)
        displayError("An error occurred whilst launching UnityTranslate!\n\nError: ${e::class.java.name}: ${e.message ?: e.localizedMessage}")
    }
}

suspend fun tryDownloadLibraries(): Collection<Path> {
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

    val libraries = mutableListOf<Path>()
    val clientJarInfo = ArtifactInfo.CODEC.decode(JsonOps.INSTANCE, metadata.getAsJsonObject("downloads").getAsJsonObject("client"))
        .orThrow.first
    val clientJarPath = librariesPath / "com/mojang/minecraft/${mcVersion}/minecraft-$mcVersion.jar"
    val clientJarDownload = DownloadHelper.queue(clientJarInfo.url.toURL(), clientJarPath, sha1 = clientJarInfo.sha1)
    downloadInfos.add(clientJarDownload)
    libraries.add(clientJarPath)

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

        val libraryPath = librariesPath / downloadData.path.orElseThrow()
        val libraryDownload = DownloadHelper.queue(downloadData.url.toURL(), libraryPath, sha1 = downloadData.sha1)
        downloadInfos.add(libraryDownload)

        libraries.add(libraryPath)
    }

    coroutineScope {
        // If we have anything we need to download, let's create a thread for making a UI.
        if (downloadInfos.any { !it.isComplete }) {
            // Launch UI thread specifically to display download progress.
            coroutineScope {
                val infosToBars = ConcurrentHashMap<DownloadInfo, Pair<JProgressBar, JLabel>>()

                launch(Dispatchers.Default) {
                    while (downloadInfos.any { !it.isComplete }) {
                        for ((info, pair) in infosToBars) {
                            val (bar, label) = pair

                            bar.string = "test"
                            bar.value = (info.progress * 100.0).toInt()
                            label.text = "${info.downloadedBytes.bytesToNearestLarge()} / ${info.totalBytes.bytesToNearestLarge()}"
                        }
                    }
                }

                launch(Dispatchers.Default) {
                    val frame = JFrame("UnityTranslate")
                    frame.iconImage = withContext(Dispatchers.IO) {
                        ImageIO.read(StandaloneConstants.ICON)
                    }

                    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                    frame.minimumSize = Dimension(600, 200)
                    frame.preferredSize = Dimension(600, 200)
                    frame.maximumSize = Dimension(600, 600)
                    frame.setLocationRelativeTo(null)

                    val panel = JPanel()
                    panel.isOpaque = false
                    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
                    panel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

                    val mainPanel = JScrollPane(panel)
                    mainPanel.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                    frame.contentPane.add(mainPanel)

                    for (info in downloadInfos) {
                        // We don't need to create something for this.
                        if (info.isComplete)
                            continue

                        val downloadPanel = JPanel()
                        downloadPanel.isOpaque = false
                        downloadPanel.layout = BoxLayout(downloadPanel, BoxLayout.Y_AXIS)
                        downloadPanel.isVisible = info.hasStarted

                        val label = JLabel("Downloading ${info.url}...")
                        label.maximumSize = Dimension(550, 20)
                        downloadPanel.add(label)

                        val progressBar = JProgressBar(0, 100)
                        progressBar.maximumSize = Dimension(1024, 20)
                        downloadPanel.add(progressBar)

                        val progressLabel = JLabel("0.00 KiB / 0.00 KiB")
                        downloadPanel.add(progressLabel)

                        infosToBars[info] = progressBar to progressLabel

                        info.onStartDownload.register {
                            downloadPanel.isVisible = true
                        }

                        info.onFinishDownload.register {
                            downloadPanel.isVisible = false
                            frame.remove(downloadPanel)

                            if (downloadInfos.all { it.isComplete }) {
                                frame.isVisible = false
                                frame.dispose()
                            }
                        }

                        panel.add(downloadPanel)
                    }

                    frame.pack()
                    frame.isVisible = true
                }
            }
        }

        downloadInfos.map { it.deferred }.awaitAll()
    }

    return libraries
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
