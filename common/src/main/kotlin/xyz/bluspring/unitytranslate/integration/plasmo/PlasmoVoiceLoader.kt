package xyz.bluspring.unitytranslate.integration.plasmo

import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.server.PlasmoVoiceServer

object PlasmoVoiceLoader {
    fun loadClient() {
        PlasmoVoiceClient.getAddonsLoader().load(PlasmoVoiceClientIntegration())
    }

    fun loadServer() {
        PlasmoVoiceServer.getAddonsLoader().load(PlasmoVoiceServerIntegration())
    }
}
