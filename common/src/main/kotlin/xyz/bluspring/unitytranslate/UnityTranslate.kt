package xyz.bluspring.unitytranslate

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.unitytranslate.plugin.PluginManager
import xyz.bluspring.unitytranslate.shared.Constants

object UnityTranslate {
    const val MOD_ID = Constants.MOD_ID
    val logger: Logger = LoggerFactory.getLogger("UnityTranslate")

    fun init() {
        PluginManager.loadPlugins()
    }
}
