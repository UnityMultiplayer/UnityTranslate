package xyz.bluspring.unitytranslate.integration

import xyz.bluspring.unitytranslate.PlatformProxy
import xyz.bluspring.unitytranslate.integration.talk_balloons.TalkBalloonsIntegration

object UnityTranslateIntegration {
    fun setup() {
        if (PlatformProxy.instance.isModLoaded("talk_balloons") && PlatformProxy.instance.isClient) {
            TalkBalloonsIntegration.setup()
        }
    }
}
