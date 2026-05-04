package xyz.bluspring.unitytranslate.standalone

import xyz.bluspring.unitytranslate.UnityTranslate

object UnityTranslateStandalone {
    @JvmStatic
    fun init() {
        try {
            UnityTranslate.init()
        } catch (e: Throwable) {
            throw HandledException(e)
        }
    }
}