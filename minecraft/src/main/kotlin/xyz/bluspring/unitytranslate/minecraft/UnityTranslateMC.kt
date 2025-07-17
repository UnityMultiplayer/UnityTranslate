package xyz.bluspring.unitytranslate.minecraft

import xyz.bluspring.unitytranslate.common.UnityTranslate
import xyz.bluspring.unitytranslate.transcriber.Transcribers

class UnityTranslateMC {
    companion object {
        val instance = UnityTranslate(MinecraftProxy.getConfigPath())

        init {
            Transcribers.init()
            instance.init()
        }
    }
}