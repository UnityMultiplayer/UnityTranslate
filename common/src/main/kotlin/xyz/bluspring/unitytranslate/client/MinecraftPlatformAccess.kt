package xyz.bluspring.unitytranslate.client

import net.minecraft.locale.Language
import xyz.bluspring.unitytranslate.api.v2.PlatformAccess

object MinecraftPlatformAccess : PlatformAccess {
    override fun translated(key: String, vararg args: Any?): String
        = Language.getInstance().getOrDefault(key).format(*args)

    override fun translatedWithFallback(key: String, fallback: String, vararg args: Any?): String
        = Language.getInstance().getOrDefault(key, fallback).format(args)
}
