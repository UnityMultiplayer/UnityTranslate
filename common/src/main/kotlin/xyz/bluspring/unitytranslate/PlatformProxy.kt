package xyz.bluspring.unitytranslate

import java.nio.file.Path
import java.util.*

interface PlatformProxy {
    val version: String
    val pluginsDir: Path

    companion object {
        val instance: PlatformProxy = ServiceLoader.load(PlatformProxy::class.java).first()
    }
}