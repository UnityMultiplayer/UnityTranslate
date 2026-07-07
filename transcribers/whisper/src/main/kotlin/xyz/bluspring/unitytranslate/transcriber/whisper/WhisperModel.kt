package xyz.bluspring.unitytranslate.transcriber.whisper

import com.mojang.serialization.Codec
import xyz.bluspring.unitytranslate.api.v2.UnityTranslateApi
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import xyz.bluspring.unitytranslate.api.v2.util.AdditionalCodecs
import java.net.URI
import java.nio.file.Path

enum class WhisperModel(val fileName: String, val minimumBytes: Long, val minimumMemoryBytes: Long) : DownloadableEntry {
    TINY("ggml-tiny.bin", 45613056, 286261248), // 43.5 MiB, 273 MiB
    BASE("ggml-base.bin", 155189248, 406847488), // 148 MiB, 388 MiB
    SMALL("ggml-small.bin", 511705088, 893386752), // 488 MiB, 852 MiB
    MEDIUM("ggml-medium.bin", 1642824990, 2254857830), // 1.53 GiB, 2.1 GiB
    LARGE("ggml-large-v3.bin", 3328599654, 4187593113), // 3.1 GiB, 3.9 GiB
    LARGE_TURBO("ggml-large-v3-turbo.bin", 1739461754, 4187593113), // 1.62 GiB, 3.9 GiB
    ;

    override val path: Path = UnityTranslateApi.instance.storagePath.resolve("models/whisper/${this.fileName}")
    override val uri: URI = URI.create("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/$fileName")

    // OpenAI models: https://github.com/openai/whisper/blob/main/whisper/__init__.py#L17-L30
    // GGML models: https://huggingface.co/ggerganov/whisper.cpp/tree/main
    // Memory usage reference: https://github.com/ggml-org/whisper.cpp#memory-usage

    companion object {
        @JvmField val CODEC: Codec<WhisperModel> = AdditionalCodecs.enumCodec(WhisperModel::valueOf)
    }
}
