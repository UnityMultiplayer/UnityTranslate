package xyz.bluspring.unitytranslate.client

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.UnityTranslateApiImpl
import xyz.bluspring.unitytranslate.api.v2.transcriber.InactiveTranscriber
import xyz.bluspring.unitytranslate.client.config.ClientConfig
import xyz.bluspring.unitytranslate.client.gui.MouseHelper
import xyz.bluspring.unitytranslate.client.renderer.UnityTranslateGui
import xyz.bluspring.unitytranslate.transcriber.TranscriberManager

object UnityTranslateClient {
    var handledFirstJoin = false
    val transcriberManager by lazy {
        TranscriberManager() // Client transcriber manager
    }

    fun init() {
        if (!PlatformProxy.instance.isStandalone()) {
            UnityTranslateMCClient.init()
        }

        if (ClientConfig.transcriber !is InactiveTranscriber) {
            runBlocking {
                launch(start = CoroutineStart.UNDISPATCHED) {
                    UnityTranslateApiImpl.setActiveTranscriber(ClientConfig.transcriber)
                }
            }
        }
    }

    fun tick() {
        UnityTranslateGui.tick()
        this.transcriberManager.tick()

        MouseHelper.tick()
    }

    fun onClose() {
        MouseHelper.close()

        runBlocking {
            for (source in UnityTranslateApiImpl.transcriberSources.values) {
                source.reset()
            }

            for (transcriber in UnityTranslateApiImpl.transcribers.values) {
                transcriber.close()
            }

            for (instance in UnityTranslateApiImpl.translators.values) {
                instance.close()
            }
        }
    }
}
