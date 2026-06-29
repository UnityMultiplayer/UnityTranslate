package xyz.bluspring.unitytranslate.client

import xyz.bluspring.unitytranslate.client.gui.MouseHelper

object UnityTranslateClient {
    var handledFirstJoin = true

    fun init() {
    }

    fun onClose() {
        MouseHelper.close()
    }
}
