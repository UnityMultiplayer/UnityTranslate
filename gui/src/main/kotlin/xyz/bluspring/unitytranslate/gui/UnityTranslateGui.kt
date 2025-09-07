package xyz.bluspring.unitytranslate.gui

import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.universal.UGraphics
import gg.essential.universal.UI18n.i18n
import gg.essential.universal.UMinecraft
import gg.essential.universal.standalone.glfw.Glfw
import gg.essential.universal.standalone.nanovg.NvgContext
import gg.essential.universal.standalone.nanovg.NvgFont
import gg.essential.universal.standalone.nanovg.NvgFontFace
import gg.essential.universal.standalone.runUniversalCraft
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.lenni0451.reflect.Agents
import net.lenni0451.reflect.accessor.UnsafeAccess
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL32C
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
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
import xyz.bluspring.unitytranslate.gui.visual.LayeredScreenManager
import xyz.bluspring.unitytranslate.gui.standalone.StandaloneI18n
import xyz.bluspring.unitytranslate.gui.standalone.gui.StandaloneScreen
import xyz.bluspring.unitytranslate.gui.transcriber.Transcribers
import xyz.bluspring.unitytranslate.gui.visual.transcript.TranscriptHolder
import java.lang.instrument.ClassFileTransformer
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.ProtectionDomain
import java.util.function.BiConsumer
import kotlin.io.path.*

object UnityTranslateGui {
    lateinit var instance: UnityTranslate
    lateinit var configFile: Path
    lateinit var transcriber: SpeechTranscriber

    var isMuted = false
    var useClientTranslations = false
    var isStandalone = false
        private set

    var clientConfig = UnityTranslateClientConfig()

    var transcriptRenderer = TranscriptBoxRenderer()
        private set
    val transcriptHolders = mutableMapOf<Language, TranscriptHolder>()

    internal fun reloadRenderers() {
        transcriptRenderer = TranscriptBoxRenderer()
        LayeredScreenManager.swap(StandaloneScreen())
    }

    fun renderClear() {
        if (!isStandalone)
            return

        GL32C.glClearColor(0f, 0f, 0f, 0f)
        GL32C.glClearDepth(1.0)
        GL32C.glClear(16384 or 256)
    }

    // We need to add some window hints
    private fun modifyInternalStuff() {
        val instrumentation = Agents.getInstrumentation()

        instrumentation.addTransformer(object : ClassFileTransformer {
            override fun transform(
                loader: ClassLoader?,
                className: String,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                classfileBuffer: ByteArray
            ): ByteArray? {
                if (className.contains("GlfwWindow")) {
                    val classReader = ClassReader(classfileBuffer)
                    val classNode = ClassNode(Opcodes.ASM9)

                    classReader.accept(classNode, 0)

                    run {
                        val initMethod = classNode.methods.first { it.name == "<init>" && it.desc == "(Ljava/lang/String;IIZ)V" }
                        val instructions = initMethod.instructions

                        val target = instructions.first { it is MethodInsnNode && it.opcode == Opcodes.INVOKESTATIC && it.owner == "org/lwjgl/glfw/GLFW" && it.name == "glfwDefaultWindowHints" && it.desc == "()V" }

                        val newInsns = InsnList()

                        // GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE)
                        newInsns.add(LabelNode())
                        newInsns.add(LdcInsnNode(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER))
                        newInsns.add(InsnNode(Opcodes.ICONST_1))
                        newInsns.add(MethodInsnNode(Opcodes.INVOKESTATIC, "org/lwjgl/glfw/GLFW", "glfwWindowHint", "(II)V", false))

                        instructions.insert(target, newInsns)

                        initMethod.instructions = instructions
                    }

                    val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
                    classNode.accept(classWriter)

                    return classWriter.toByteArray()
                }

                return super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer)
            }
        }, true)
    }

    @JvmStatic
    fun main(args: Array<out String>) {
        isStandalone = true
        modifyInternalStuff()

        instance = UnityTranslate(Path("."))
        configFile = Path("./unitytranslate.json")

        init()

        // otherwise it's fuckin' tiny
        UMinecraft.guiScale = 2

        runUniversalCraft("UnityTranslate", 854, 480) { window ->
            window.uiScope.launch {
                // Load window icon

                val iconResource = UnityTranslateGui::class.java.classLoader.getResource("icon_standalone.png")!!
                var icon: ByteBuffer? = null

                try {
                    MemoryStack.stackPush().use { stack ->
                        val width = stack.mallocInt(1)
                        val height = stack.mallocInt(1)
                        val channels = stack.mallocInt(1)

                        val byteArray = iconResource.readBytes()
                        val buffer = ByteBuffer.allocateDirect(byteArray.size).put(byteArray).flip()
                        icon = STBImage.stbi_load_from_memory(buffer, width, height, channels, 4)

                        val reason = STBImage.stbi_failure_reason()
                        if (reason != null)
                            logger.error("Failed to load icon from memory: $reason")

                        if (icon == null)
                            return@use

                        withContext(Dispatchers.Glfw) {
                            GLFW.glfwSetWindowIcon(window.glfwWindow.glfwId, GLFWImage.malloc(1, stack)
                                .width(width.get(0))
                                .height(height.get(0))
                                .pixels(icon!!)
                            )
                        }
                    }
                } finally {
                    if (icon != null) {
                        STBImage.stbi_image_free(icon!!)
                    }
                }
            }

            GLFW.glfwSetWindowCloseCallback(window.glfwWindow.glfwId) {
                shutdown()
            }

            // Forcefully load our font instead
            setFont()

            LayeredScreenManager.open(StandaloneScreen())
            window.renderScreenUntilClosed()
        }
    }

    fun setFont(style: String = "Regular") {
        val fontField = UGraphics::class.java.getDeclaredField("MC_FONT")
        fontField.isAccessible = true

        val fieldBase = UnsafeAccess.staticFieldBase(fontField)
        val fieldOffset = UnsafeAccess.staticFieldOffset(fontField)

        val font = NvgFont(NvgFontFace(NvgContext(),
            UnityTranslateGui::class.java.getResource("/fonts/TikTokSans-$style.ttf")!!.readBytes()),
            8f, 6f, 1f
        )
        UnsafeAccess.putObject(fieldBase, fieldOffset, font)
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