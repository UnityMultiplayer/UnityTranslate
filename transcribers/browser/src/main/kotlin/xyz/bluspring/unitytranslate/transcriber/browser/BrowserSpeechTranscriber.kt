package xyz.bluspring.unitytranslate.transcriber.browser

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import gg.essential.universal.UDesktop
import gg.essential.universal.UI18n
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.util.HttpUtil
import xyz.bluspring.unitytranslate.transcriber.api.SpeechTranscriber
import xyz.bluspring.unitytranslate.transcriber.api.events.TranscriberEvents
import java.net.InetSocketAddress

class BrowserSpeechTranscriber(language: Language) : SpeechTranscriber(language) {
    override var language: Language
        get() = super.language
        set(value) {
            super.language = value
            this.socket.broadcast("set_language", JsonObject().apply {
                addProperty("language", language.asTranscriberCode)
            })
        }

    // https://r12a.github.io/app-subtags/
    override val Language.asTranscriberCode: String
        get() = when (this) {
            Language.ENGLISH -> "en-US"
            Language.SPANISH -> "es-013"
            Language.PORTUGUESE -> "pt-BR"
            Language.FRENCH -> "fr"
            Language.SWEDISH -> "sv"
            Language.MALAY -> "ms"
            Language.HEBREW -> "he"
            Language.CHINESE -> "cmn"
            Language.CHINESE_TRADITIONAL -> "cmn" // TODO: this is probably not correct!
            else -> this.code // In most cases, the language is just wrapped like this
        }

    // The browser is capable of supporting all languages, so...
    override val supportedLanguages: Set<Language> = Language.entries.toSet()

    val socketPort = HttpUtil.availablePort
    val server: HttpServer
    val socket = BrowserSocket()
    val serverPort = if (!HttpUtil.isPortAvailable(25117))
        HttpUtil.availablePort
    else
        25117
    val browserApplication = BrowserApplication(UnityTranslate.instance)

    var driver: WebDriver

    init {
        browserApplication.socketPort = socketPort
        server = HttpServer.create(InetSocketAddress("0.0.0.0", serverPort), 0)
        browserApplication.addHandler(server)

        server.start()

        socket.isDaemon = true
        socket.start()

        val type = UTBrowserType.CHROME
        val path = type.findExistingPath()?.toFile() ?: throw IllegalArgumentException("No browser path found for browser type $type!")

        driver = if (type == UTBrowserType.CHROME) {
            ChromeDriver(ChromeOptions().apply {
                this.addArguments(
                    "--headless=new",
                    "--disable-user-media-security=true",
                    "--use-fake-ui-for-media-stream"
                )
                this.setBinary(path)
            })
        } else if (type == UTBrowserType.EDGE) {
            EdgeDriver(EdgeOptions().apply {
                this.addArguments(
                    "--headless=new",
                    "--disable-user-media-security=true",
                    "--use-fake-ui-for-media-stream"
                )
                this.setBinary(path)
            })
        } else {
            throw IllegalStateException()
        }

        // Try making the window a child of Minecraft.
        run {
            if (UDesktop.isWindows) {
                BrowserNativeHandles.makeGameParentWindows(driver as RemoteWebDriver)
            } else if (UDesktop.isMac) {
                BrowserNativeHandles.makeGameParentOsx(driver as RemoteWebDriver)
            } else if (UDesktop.isLinux) {
                BrowserNativeHandles.makeGameParentLinux(driver as RemoteWebDriver)
            }
        }

        driver.get("http://127.0.0.1:$serverPort")
    }

    override fun stop() {
        driver.close()
        driver.quit()

        server.stop(0)
        socket.stop(1000)
    }

    override var isMuted: Boolean = false
        set(value) {
            field = value
            this.socket.broadcast("set_muted", JsonObject().apply {
                this.addProperty("muted", value)
            })
        }

    inner class BrowserSocket : WebSocketServer(InetSocketAddress("0.0.0.0", socketPort)) {
        var totalConnections = 0

        override fun onOpen(ws: WebSocket, handshake: ClientHandshake) {
            ws.sendData("set_language", JsonObject().apply {
                addProperty("language", language.asTranscriberCode)
            })
            totalConnections++

            TranscriberEvents.STARTED.invoker().onTranscriptEvent(this@BrowserSpeechTranscriber)
            isMuted = isMuted // Resend the mute state
        }

        override fun onClose(ws: WebSocket, code: Int, reason: String, remote: Boolean) {
            totalConnections--

            TranscriberEvents.STOPPED.invoker().onTranscriptEvent(this@BrowserSpeechTranscriber)
            driver.navigate().refresh()
        }

        override fun onMessage(ws: WebSocket, message: String) {
            val msg = JsonParser.parseString(message).asJsonObject
            val data = if (msg.has("d")) msg.getAsJsonObject("d") else JsonObject()

            when (msg.get("op").asString) {
                "transcript" -> {
                    val results = data.getAsJsonArray("results")
                    val index = data.get("index").asInt

                    val deserialized = mutableListOf<Pair<String, Double>>()
                    for (result in results) {
                        val d = result.asJsonObject
                        deserialized.add(d.get("text").asString to d.get("confidence").asDouble)
                    }

                    if (deserialized.isEmpty()) {
                        lastIndex = currentOffset + index
                        return
                    }

                    val selected = deserialized.sortedByDescending { it.second }[0].first

                    if (selected.isNotBlank()) {
                        TranscriberEvents.UPDATED.invoker().onTranscriptUpdated(this@BrowserSpeechTranscriber, currentOffset + index, selected.trim())
                    }

                    lastIndex = currentOffset + index
                }

                "reset" -> {
                    currentOffset = lastIndex + 1
                }

                "error" -> {
                    val type = data.get("type").asString

                    if (type == "too_many_resets") {
                        driver.navigate().refresh()
                    }

                    TranscriberEvents.ERROR.invoker().onTranscriptError(this@BrowserSpeechTranscriber,
                        Exception(UI18n.i18n("unitytranslate.transcriber.browser.error.$type")))
                }
            }
        }

        override fun onError(ws: WebSocket, ex: Exception) {
            ex.printStackTrace()
        }

        override fun onStart() {
            UnityTranslate.logger.info("Started WebSocket server for Browser Transcriber mode at ${this.address}")
        }

        fun broadcast(op: String, data: JsonObject = JsonObject()) {
            super.broadcast(JsonObject().apply {
                this.addProperty("op", op)
                this.add("d", data)
            }.toString())
        }

        fun WebSocket.sendData(op: String, data: JsonObject = JsonObject()) {
            this.send(JsonObject().apply {
                this.addProperty("op", op)
                this.add("d", data)
            }.toString())
        }
    }
}