package xyz.bluspring.unitytranslate.transcriber.whisper

import com.mojang.serialization.Codec
import com.sun.management.OperatingSystemMXBean
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.config.NameProvidingEntry
import xyz.bluspring.unitytranslate.api.v2.config.TooltipProvidingEntry
import xyz.bluspring.unitytranslate.api.v2.config.TranslatableTooltip
import xyz.bluspring.unitytranslate.api.v2.download.DownloadHelper.bytesToNearestLarge
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import xyz.bluspring.unitytranslate.api.v2.util.ARGBHelper
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs
import java.lang.management.ManagementFactory
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists

enum class WhisperModel(val fileName: String, val minimumBytes: Long, val minimumMemoryBytes: Long) : DownloadableEntry, NameProvidingEntry, TooltipProvidingEntry {
    TINY("ggml-tiny.bin", 77_691_713, 286261248), // 74.0 MiB, 273 MiB
    BASE("ggml-base.bin", 147_951_465, 406847488), // 141 MiB, 388 MiB
    SMALL("ggml-small.bin", 487_601_967, 893386752), // 465 MiB, 852 MiB
    MEDIUM("ggml-medium.bin", 1_533_763_059, 2254857830), // 1.42 GiB, 2.1 GiB
    LARGE("ggml-large-v3.bin", 3_095_033_483, 4187593113), // 2.88 GiB, 3.9 GiB
    LARGE_TURBO("ggml-large-v3-turbo.bin", 1_624_555_275, 4187593113), // 1.51 GiB, 3.9 GiB
    ;

    override val path: Path = UnityTranslateApi.instance.storagePath.resolve("models/transcriber/whisper/${this.fileName}")
    override val uri: URI = URI.create("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$fileName")

    override val serializedName: String = this.name.lowercase()

    override fun compareTo(other: NameProvidingEntry): Int {
        if (other is WhisperModel)
            return this.ordinal.compareTo(other.ordinal)

        return this.serializedName.compareTo(other.serializedName)
    }

    val hasEnoughMemory: Boolean
        get() {
            val bean = ManagementFactory.getOperatingSystemMXBean() as OperatingSystemMXBean
            val totalMemory = bean.totalMemorySize
            val usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

            // Not enough memory!
            return totalMemory - usedMemory >= this.minimumMemoryBytes
        }

    val hasEnoughStorage: Boolean
        get() {
            val path = if (this.path.exists())
                this.path
            else if (this.path.parent.exists())
                this.path.parent
            else {
                this.path.createParentDirectories()
                this.path.parent
            }

            return Files.getFileStore(path).usableSpace >= this.minimumBytes
        }

    override val tooltip: Collection<TranslatableTooltip>
        get() {
            return listOf(
                TranslatableTooltip(""),
                TranslatableTooltip("unitytranslate.model.estimated.storage", listOf(this.minimumBytes.bytesToNearestLarge()), if (this.hasEnoughStorage) -1 else ARGBHelper.color(255, 255, 15, 15)),
                TranslatableTooltip("unitytranslate.model.estimated.memory", listOf(this.minimumMemoryBytes.bytesToNearestLarge()), if (this.hasEnoughMemory) -1 else ARGBHelper.color(255, 255, 15, 15)),
            )
        }

    override fun canBeSelected(): Boolean {
        return this.hasEnoughMemory && this.hasEnoughStorage
    }

    // OpenAI models: https://github.com/openai/whisper/blob/main/whisper/__init__.py#L17-L30
    // GGML models: https://huggingface.co/ggerganov/whisper.cpp/tree/main
    // Memory usage reference: https://github.com/ggml-org/whisper.cpp#memory-usage

    companion object {
        @JvmField val CODEC: Codec<WhisperModel> = AdditionalCodecs.enumCodec(WhisperModel::valueOf)
    }
}
