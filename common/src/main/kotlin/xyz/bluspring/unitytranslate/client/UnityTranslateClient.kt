package xyz.bluspring.unitytranslate.client

import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.integration.talk_balloons.TalkBalloonsIntegration

object UnityTranslateClient {
    fun init() {
        if (PlatformProxy.instance.isModLoaded("talk_balloons")) {
            TalkBalloonsIntegration.setup()
        }
    }
}
