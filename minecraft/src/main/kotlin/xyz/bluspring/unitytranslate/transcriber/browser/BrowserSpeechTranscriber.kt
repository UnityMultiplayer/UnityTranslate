package xyz.bluspring.unitytranslate.transcriber.browser

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.microsoft.playwright.*
import com.sun.net.httpserver.HttpServer
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.common.transcriber.SpeechTranscriber
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.common.util.HttpUtil
import xyz.bluspring.unitytranslate.minecraft.MinecraftProxy
import xyz.bluspring.unitytranslate.minecraft.client.UnityTranslateMCClient
import java.net.InetSocketAddress

class BrowserSpeechTranscriber(instance: UnityTranslate, language: Language) : SpeechTranscriber(instance, language) {
    val socketPort = HttpUtil.availablePort
    val server: HttpServer
    val socket = BrowserSocket()
    val serverPort = if (!HttpUtil.isPortAvailable(25117))
        HttpUtil.availablePort
    else
        25117
    val browserApplication = BrowserApplication(instance)

    var playwright = Playwright.create()
    var browser: Browser
    var context: BrowserContext
    var page: Page

    init {
        browserApplication.socketPort = socketPort
        server = HttpServer.create(InetSocketAddress("0.0.0.0", serverPort), 0)
        browserApplication.addHandler(server)

        server.start()

        socket.isDaemon = true
        socket.start()

        playwright = Playwright.create()
        // Focus on trying to use Chromium, as other browsers don't support the Web Speech API.
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().apply {
            this.executablePath = UTBrowserType.CHROME.findExistingPath()
            this.chromiumSandbox = true // Sandbox Chromium, we don't want people to hack outside if an exploit is found.
            this.headless = true // Run in headless mode, so a new browser window won't be opened.
        })
        context = browser.newContext(Browser.NewContextOptions().apply {
            this.permissions = listOf("microphone")
        })
        page = context.newPage()
        page.navigate("http://127.0.0.1:$serverPort")
    }

    override fun stop() {
        page.close()
        context.close()
        browser.close()
        playwright.close()

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

            UnityTranslateMCClient.displayMessage(MinecraftProxy.translatable("unitytranslate.transcriber.browser.connected"))
            setMuted(UnityTranslateMCClient.isMuted)
        }

        override fun onClose(ws: WebSocket, code: Int, reason: String, remote: Boolean) {
            totalConnections--

            UnityTranslateMCClient.displayMessage(MinecraftProxy.translatable("unitytranslate.transcriber.browser.disconnected"))
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
                        page.reload()
                    }

                    UnityTranslateMCClient.displayMessage(MinecraftProxy.translatable("unitytranslate.transcriber.error")
                        .append(MinecraftProxy.translatable("unitytranslate.transcriber.browser.error.$type")), true)
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