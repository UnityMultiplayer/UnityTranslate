package xyz.bluspring.unitytranslate.gui.transcriber.browser

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
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.common.util.HttpUtil
import xyz.bluspring.unitytranslate.gui.UnityTranslateGui
import xyz.bluspring.unitytranslate.gui.events.TranscriberEvents
import java.net.InetSocketAddress

class BrowserSpeechTranscriber(instance: UnityTranslate, language: Language) : SpeechTranscriber(TranscriberType.BROWSER, instance, language) {
    val socketPort = HttpUtil.availablePort
    val server: HttpServer
    val socket = BrowserSocket()
    val serverPort = if (!HttpUtil.isPortAvailable(25117))
        HttpUtil.availablePort
    else
        25117
    val browserApplication = BrowserApplication(instance)

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

    override fun changeLanguage(language: Language) {
        super.changeLanguage(language)
        this.socket.broadcast("set_language", JsonObject().apply {
            addProperty("language", language.supportedTranscribers[TranscriberType.BROWSER])
        })
    }

    override fun setMuted(muted: Boolean) {
        this.socket.broadcast("set_muted", JsonObject().apply {
            this.addProperty("muted", muted)
        })
    }

    inner class BrowserSocket : WebSocketServer(InetSocketAddress("0.0.0.0", socketPort)) {
        var totalConnections = 0

        override fun onOpen(ws: WebSocket, handshake: ClientHandshake) {
            ws.sendData("set_language", JsonObject().apply {
                addProperty("language", language.supportedTranscribers[TranscriberType.BROWSER])
            })
            totalConnections++

            TranscriberEvents.TRANSCRIBER_STARTED.invoker().onTranscriptEvent(this@BrowserSpeechTranscriber.type)
            setMuted(UnityTranslateGui.isMuted)
        }

        override fun onClose(ws: WebSocket, code: Int, reason: String, remote: Boolean) {
            totalConnections--

            TranscriberEvents.TRANSCRIBER_STOPPED.invoker().onTranscriptEvent(this@BrowserSpeechTranscriber.type)
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
                        updater.accept(currentOffset + index, selected.trim())
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

                    TranscriberEvents.TRANSCRIBER_ERROR.invoker().onTranscriptError(this@BrowserSpeechTranscriber.type,
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