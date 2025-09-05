package xyz.bluspring.unitytranslate.gui

import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.minus
import gg.essential.elementa.dsl.percentOfWindow
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UI18n.i18n
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMinecraft
import gg.essential.universal.UScreen
import gg.essential.universal.standalone.runUniversalCraft
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.lwjgl.glfw.GLFW
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.UnityTranslate.Companion.json
import xyz.bluspring.unitytranslate.common.UnityTranslate.Companion.logger
import xyz.bluspring.unitytranslate.common.events.TranscriptEvents
import xyz.bluspring.unitytranslate.common.holders.PlayerHolder
import xyz.bluspring.unitytranslate.common.network.v1.serverbound.V1ServerboundSendTranscriptPacket
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.translator.Transcript
import xyz.bluspring.unitytranslate.gui.config.UnityTranslateClientConfig
import xyz.bluspring.unitytranslate.gui.menu.LayeredScreenManager
import xyz.bluspring.unitytranslate.gui.standalone.StandaloneI18n
import xyz.bluspring.unitytranslate.gui.standalone.gui.StandaloneScreen
import xyz.bluspring.unitytranslate.gui.transcriber.Transcribers
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.function.BiConsumer
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.writeText

object UnityTranslateGui {
    lateinit var instance: UnityTranslate
    lateinit var configFile: Path
    lateinit var transcriber: SpeechTranscriber

    var isMuted = false
    var useClientTranslations = false


    var clientConfig = UnityTranslateClientConfig()

    var transcriptRenderer = TranscriptBoxRenderer()
        private set
    val transcriptHolders = mutableMapOf<Language, TranscriptHolder>()

    internal fun reloadRenderers() {
        transcriptRenderer = TranscriptBoxRenderer()
        LayeredScreenManager.swap(StandaloneScreen())
    }

    @JvmStatic
    fun main(args: Array<out String>) {
        instance = UnityTranslate(Path("."))
        configFile = Path("./unitytranslate.json")

        init()

        // otherwise it's fuckin' tiny
        //UMinecraft.guiScale = 2

        runBlocking {
            withContext(Dispatchers.Main) {
                GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE) // make window transparent
            }
        }

        runUniversalCraft("UnityTranslate", 854, 480) { window ->
            GLFW.glfwSetWindowCloseCallback(window.glfwWindow.glfwId) {
                shutdown()
            }

            LayeredScreenManager.open(StandaloneScreen())
            window.renderScreenUntilClosed()
        }
    }

    fun init() {
        UnityTranslate.instance.init()
        loadConfig()
        Transcribers.init()

        updateConfig()
        StandaloneI18n.loadLanguage("en_us")
    }

    fun shutdown() {
        transcriber.stop()
        UnityTranslate.instance.translatorManager.shutdown()
    }

    fun saveConfig() {
        try {
            if (!this.configFile.parent.exists())
                this.configFile.createParentDirectories()

            if (!this.configFile.exists())
                this.configFile.createFile()

            val serialized = json.encodeToString(
                UnityTranslateClientConfig.serializer(),
                clientConfig
            )

            this.configFile.writeText(serialized, Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        } catch (e: Exception) {
            logger.error("Failed to save UnityTranslate config!")
            e.printStackTrace()
        }

        UnityTranslate.instance.saveConfig()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun loadConfig() {
        if (!configFile.exists()) {
            clientConfig = UnityTranslateClientConfig()
            return
        }

        try {
            clientConfig = json.decodeFromStream(UnityTranslateClientConfig.serializer(), configFile.inputStream(StandardOpenOption.READ))
        } catch (e: Exception) {
            logger.error("Failed to load UnityTranslate config, reverting to defaults.")
            clientConfig = UnityTranslateClientConfig()
            e.printStackTrace()
        }
    }

    fun updateConfig() {
        try {
            if (transcriber.type != clientConfig.transcriber) {
                transcriber.stop()
                transcriber = clientConfig.transcriber.creator.invoke(UnityTranslate.instance, clientConfig.spokenLanguage)
            }
        } catch (_: Throwable) {
            transcriber = clientConfig.transcriber.creator.invoke(UnityTranslate.instance, clientConfig.spokenLanguage)
        }
        transcriber.changeLanguage(clientConfig.spokenLanguage)
        setupTranscriber(transcriber, PlayerHolder.EMPTY)

        transcriptHolders.filter { clientConfig.transcriptBoxes.none { b -> b.language == it.key } && clientConfig.spokenLanguage != it.key && clientConfig.balloonLanguage != it.key }
            .forEach { (language, _) ->
                transcriptHolders.remove(language)
            }

        for (config in clientConfig.transcriptBoxes) {
            if (!transcriptHolders.contains(config.language))
                transcriptHolders[config.language] = TranscriptHolder(config.language)
        }

        if (!transcriptHolders.contains(clientConfig.spokenLanguage)) {
            transcriptHolders[clientConfig.spokenLanguage] = TranscriptHolder(
                clientConfig.spokenLanguage)
        }

        if (clientConfig.balloonLanguage != null && !transcriptHolders.contains(
                clientConfig.balloonLanguage)) {
            transcriptHolders[clientConfig.balloonLanguage!!] = TranscriptHolder(
                clientConfig.balloonLanguage!!)
        }

        transcriptRenderer.update()
    }

    fun setupTranscriber(transcriber: SpeechTranscriber, player: PlayerHolder) {
        transcriber.updater = BiConsumer { index, text ->
            if (isMuted)
                return@BiConsumer

            val updateTime = System.currentTimeMillis()

            if (UnityTranslate.instance.proxy.serverSupportsTranslations()) {
                UnityTranslate.instance.proxy.sendPacketClient(V1ServerboundSendTranscriptPacket(UnityTranslateGui.transcriber.language, index, updateTime, text))
            } else {
                val translatorManager = UnityTranslate.instance.translatorManager

                for ((language, holder) in transcriptHolders) {
                    translatorManager.scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        val translated = translatorManager.queueTranslation(text, transcriber.language, language, player.uuid, index)
                        holder.updateTranscript(player, translated ?: text, transcriber.language, index, updateTime, translated == null)
                    }
                }
            }

            val holder = transcriptHolders[transcriber.language]
            holder?.updateTranscript(player, text, transcriber.language, index, updateTime, false)

            if (holder == null) {
                TranscriptEvents.UPDATE.invoker().onTranscriptUpdate(Transcript(index, player, text, transcriber.language, updateTime, false), transcriber.language)
            }
        }
    }

    fun Window.addCreditText() {
        val version = UnityTranslate.instance.proxy.modVersion

        UIText("UnityTranslate v$version").constrain {
            this.x = 2.pixels
            this.y = 100.percentOfWindow - 20.pixels
        } childOf this

        UIText(i18n("unitytranslate.credit.author")).constrain {
            this.x = 2.pixels
            this.y = 100.percentOfWindow - 10.pixels
        } childOf this
    }
}