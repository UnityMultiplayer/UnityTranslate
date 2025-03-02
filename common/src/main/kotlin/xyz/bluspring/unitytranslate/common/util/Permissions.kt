package xyz.bluspring.unitytranslate.common.util

import xyz.bluspring.unitytranslate.common.UnityTranslate

object Permissions {
    @JvmStatic
    private fun permission(name: String): String {
        return "${UnityTranslate.MOD_ID}.$name"
    }

    @JvmField val REQUEST_TRANSLATIONS = permission("request_translations")
    @JvmField val RELOAD_COMMAND = permission("commands.reload")
    @JvmField val CLEAR_QUEUE_COMMAND = permission("commands.clearqueue")
    @JvmField val INFO_COMMAND = permission("commands.info")
    @JvmField val MODIFY_CONFIG = permission("modify_config")

    val permissions = setOf(
        REQUEST_TRANSLATIONS, RELOAD_COMMAND, CLEAR_QUEUE_COMMAND, INFO_COMMAND, MODIFY_CONFIG
    )
}