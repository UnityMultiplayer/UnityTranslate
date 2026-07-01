package xyz.bluspring.unitytranslate.client

import xyz.bluspring.unitytranslate.client.gui.MouseHelper
import xyz.bluspring.unitytranslate.client.renderer.UnityTranslateGui

object UnityTranslateClient {
    var handledFirstJoin = false

    fun init() {
    }

    fun tick() {
        UnityTranslateGui.tick()
    }

    fun onClose() {
        MouseHelper.close()
    }
}
