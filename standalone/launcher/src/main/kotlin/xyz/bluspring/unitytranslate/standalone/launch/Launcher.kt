package xyz.bluspring.unitytranslate.standalone.launch

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHash
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper.bytesToNearestLarge
import xyz.bluspring.unitytranslate.api.v2.download.DownloadInfo
import xyz.bluspring.unitytranslate.shared.*
import xyz.bluspring.unitytranslate.shared.update.UpdateHelper
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
val runtimePath = Path("runtime")
val downloadedDataPath = Path("metadata/unitytranslate.json")
val launchConfigPath = Path("launcher_config.json")

val logger: Logger = LoggerFactory.getLogger("UnityTranslate Launcher")
var downloadedData: DownloadedData? = if (downloadedDataPath.exists())
    runCatching { DownloadedData.get(JsonParser.parseReader(downloadedDataPath.bufferedReader(options = arrayOf(StandardOpenOption.READ)))) }.getOrNull()
else null
val launchConfig: LaunchConfig = (if (launchConfigPath.exists())
    runCatching { LaunchConfig.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(launchConfigPath.bufferedReader(options = arrayOf(StandardOpenOption.READ)))).orThrow.first }.getOrNull()
else
    null) ?: LaunchConfig()

private fun displayError(message: String) {
    Toolkit.getDefaultToolkit().beep()
    val optionPane = JOptionPane(message, JOptionPane.ERROR_MESSAGE)
    val dialog = optionPane.createDialog("UnityTranslate")

    dialog.setIconImage(ImageIO.read(Constants.ICON_STANDALONE))
    dialog.isVisible = true
    dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    exitProcess(1)
}

private fun displayUpdateError(message: String, url: String) {
    Toolkit.getDefaultToolkit().beep()
    val optionPane = JOptionPane(message, JOptionPane.ERROR_MESSAGE)
    optionPane.add(JButton("Ok").apply {
        addActionListener { event ->
            OperatingSystem.type.openUri(URI.create(url))
        }
    })
    val dialog = optionPane.createDialog("UnityTranslate")

    dialog.setIconImage(ImageIO.read(Constants.ICON_STANDALONE))
    dialog.isVisible = true
    dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    exitProcess(1)
}

fun buildUnityTranslatePath(version: String): Path {
    return librariesPath / "xyz/bluspring/unitytranslate/unitytranslate-standalone/$version/unitytranslate-standalone-$version.jar"
}

fun main() {
    if (GraphicsEnvironment.isHeadless()) {
        throw IllegalStateException("You're running UnityTranslate in a headless environment, which is not supported!")
    }

    logger.info("Launching UnityTranslate...")

    // Print info so logs provide sufficient info behind problems.
    logger.info("Operating System: ${System.getProperty("os.name")} (${System.getProperty("os.arch")})")
    logger.info("Launcher Version: ${LauncherMeta.version} (${LauncherMeta.buildHash})")
    logger.info("Launcher Java Version: ${LauncherMeta.javaVersion}")
    logger.info("Current Java Version: ${System.getProperty("java.vendor")} ${Runtime.version()} (${System.getProperty("java.runtime.name")}, ${System.getProperty("java.vm.name")})")
    logger.info("Installed UnityTranslate version: ${downloadedData?.currentVersion ?: "(none)"}")
    logger.info("Installed Minecraft version: ${downloadedData?.minecraftVersion ?: "(none)"}")

    try {
        runBlocking {
            val unityTranslateClass = runCatching { Class.forName("xyz.bluspring.unitytranslate.UnityTranslate") }.getOrNull()
            var javaPath: String = ProcessHandle.current().info().command().orElseThrow()
            lateinit var metadata: Metadata

            // Download all libraries and everything will be fine :D
            val currentDownloadedData = downloadedData
            val libraries = if (unityTranslateClass != null) {
                metadata = Metadata.parse(JsonParser.parseString(unityTranslateClass.getResource("/metadata.json")!!.readText()).asJsonObject)
                val minecraftVersion = metadata.minecraftVersion

                // We're most likely in the development environment, let's set up for that.
                tryDownloadLibraries(metadata.version, minecraftVersion, false)
            } else {
                // We're not in a development environment, let's make sure everything is correct first.
                var needsJava = false
                if (Runtime.version().feature() < 25) {
                    // We don't have Java 25 or newer, let's try to download it.
                    val downloadedJavaPath = runtimePath / LauncherMeta.javaVersion
                    val runtimePath = downloadedJavaPath / "bin/java${if (OperatingSystem.type == OperatingSystem.Type.WINDOWS) "w.exe" else ""}"

                    javaPath = runtimePath.absolutePathString()
                    if (runtimePath.exists()) {
                        // We already have it downloaded, we don't need to worry.
                    } else {
                        // Oh boy, we need to download it.
                        needsJava = true
                    }
                }

                tryDownloadLibraries(downloadedData?.currentVersion, downloadedData?.minecraftVersion, needsJava)
            }

            // Update the downloaded data
            if (downloadedData != null && downloadedData != currentDownloadedData) {
                downloadedDataPath.writeText(DownloadedData.CODEC.encodeStart(JsonOps.INSTANCE, downloadedData).orThrow.toString())
            }

            if (unityTranslateClass != null) {
                // See, told you it'd be fine! Time to load all the libraries and launch.
                val urls = listOfNotNull(
                    UnityTranslateApi::class.java.protectionDomain.codeSource.location,

                    runCatching { Class.forName("xyz.bluspring.unitytranslate.shared.Constants") }.getOrNull()?.protectionDomain?.codeSource?.location,
                    unityTranslateClass.protectionDomain.codeSource.location,
                    runCatching { Class.forName("xyz.bluspring.unitytranslate.standalone.UnityTranslateStandalone") }.getOrNull()?.protectionDomain?.codeSource?.location,
                )
                val classLoader = URLClassLoader((urls + libraries.paths.map { it.toUri().toURL() }).toTypedArray(), FilteredClassLoader)

                Thread.currentThread().contextClassLoader = classLoader

                // We're gonna use reflection to launch, so we are able to use the correct class loader.
                val standaloneClass = classLoader.loadClass("xyz.bluspring.unitytranslate.standalone.UnityTranslateStandalone")
                standaloneClass.getDeclaredMethod("init").invoke(null)
            } else {
                // TODO: actually set up loading in production
            }
        }
    } catch (e: NeedsUpdateException) {
        logger.error("UnityTranslate Launcher requires an update!", e)
        displayUpdateError(e.message ?: e.localizedMessage, e.url)
    } catch (e: HandledException) {
        logger.error("UnityTranslate has crashed!", e)
        exitProcess(1)
    } catch (e: Throwable) {
        // it wasn't fine D:
        logger.error("An error occurred whilst launching UnityTranslate!", e)
        displayError("An error occurred whilst launching UnityTranslate!\n\nError: ${e::class.java.name}: ${e.message ?: e.localizedMessage}")
    }
}

data class DownloadData(
    val paths: Collection<Path>,
    val minecraftVersion: String,
    val unityTranslateVersion: String
)

suspend fun tryDownloadLibraries(unityTranslateVersion: String?, minecraftVersion: String? = null, downloadJava: Boolean = false): DownloadData {
    var mcVersion = minecraftVersion
    var utVersion = unityTranslateVersion
    val downloadInfos = mutableListOf<DownloadInfo>()

    val hasUpdate = try {
        UpdateHelper.checkHasUpdate(unityTranslateVersion)
    } catch (e: Throwable) {
        if (utVersion == null)
            throw e

        false
    }

    // Java downloading from Microsoft, because their Java is tuned specifically for Minecraft.
    if (downloadJava) {
        val javaUrlString = LauncherConstants.buildJava(LauncherMeta.javaVersion)

        if (javaUrlString != null) {
            val javaUrl = URI.create(javaUrlString).toURL()
            val hash = URI.create("$javaUrlString.sha256sum.txt").toURL().readText().take(64)

            downloadInfos.add(DownloadHelper.queue(javaUrl, runtimePath / "microsoft-${LauncherMeta.javaVersion}", hash = DownloadHash.Sha256(hash)))
        } else {
            logger.warn("Could not find a suitable Java version to download! You're on your own here!")
        }
    }

    // If UnityTranslate has an update, download it.
    if (hasUpdate) {
        val updateData = UpdateHelper.getLatestUpdateData(launchConfig)

        if (updateData != null && updateData.launcherVersion.isPresent && UpdateHelper.isVersionNewer(LauncherMeta.version, updateData.launcherVersion.orElseThrow())) {
            throw NeedsUpdateException("The UnityTranslate Standalone Launcher needs to be updated before you can continue! (latest version is ${updateData.launcherVersion.orElseThrow()}, you have ${LauncherMeta.version})", Constants.LATEST_RELEASE_URL)
        }

        val standaloneUrls = updateData?.downloads?.get("standalone") ?: emptyList()

        if (updateData?.standaloneVersion?.isPresent == true)
            mcVersion = updateData.standaloneVersion.orElseThrow()

        if (standaloneUrls.isNotEmpty()) {
            utVersion = updateData?.version
            if (updateData?.standaloneVersion?.isPresent == true)
                mcVersion = updateData.standaloneVersion.orElseThrow()

            val url = DownloadHelper.findSuitableUrl(standaloneUrls)

            if (url != null && utVersion != null) {
                logger.info("Downloading UnityTranslate Standalone v${utVersion}...")
                downloadInfos.add(DownloadHelper.queue(url.url, buildUnityTranslatePath(utVersion), sha1 = url.sha1.orElse(null)))
            }
        }
    }

    if (utVersion == null || mcVersion == null) {
        throw IllegalStateException("Failed to update UnityTranslate!")
    }

    downloadedData = DownloadedData(utVersion, mcVersion)

    // Download Minecraft, because we're using it as a library at the moment.
    val mcMetaPath = metaPath / "${mcVersion}.json"
    if (!mcMetaPath.exists()) {
        logger.info("No metadata found for ${mcVersion}, downloading...")

        val manifestUrl = URI.create(LauncherConstants.MC_VERSION_MANIFEST).toURL()
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
                        ImageIO.read(Constants.ICON_STANDALONE)
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

    return DownloadData(
        libraries,
        mcVersion, utVersion
    )
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
