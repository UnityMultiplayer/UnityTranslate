package xyz.bluspring.unitytranslate.standalone

import xyz.bluspring.unitytranslate.UnityTranslate

object UnityTranslateStandalone {
    val ICON = this::class.java.getResource("/icon_standalone.png")!!

    fun init() {
        UnityTranslate.init()
    }
}