package xyz.bluspring.unitytranslate.standalone

import xyz.bluspring.unitytranslate.PlatformProxy
import java.nio.file.Path
import kotlin.io.path.Path

class StandalonePlatformProxy : PlatformProxy {
    override val version: String = UnityTranslateStandalone.metadata.version
    override val pluginsDir: Path = Path("plugins")
}
