package xyz.bluspring.unitytranslate.standalone

import xyz.bluspring.unitytranslate.PlatformProxy
import java.nio.file.Path
import kotlin.io.path.Path

class StandalonePlatformProxy : PlatformProxy {
    override val version: String = Metadata.get().version
    override val pluginsDir: Path = Path("plugins")
}