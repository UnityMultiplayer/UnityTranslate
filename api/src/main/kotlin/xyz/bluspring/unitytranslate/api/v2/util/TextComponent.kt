package xyz.bluspring.unitytranslate.api.v2.util

import net.minecraft.network.chat.Component
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi

object TextComponent {
    @JvmStatic
    fun literal(text: String): Component = UnityTranslateApi.instance.platform
}
