package xyz.bluspring.unitytranslate.transcriber.whisper

enum class WhisperModel(val url: String, val minimumBytes: Long, val minimumMemoryBytes: Long) {
    TINY("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin", 45613056, 286261248), // 43.5 MiB, 273 MiB
    BASE("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin", 155189248, 406847488), // 148 MiB, 388 MiB
    SMALL("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-small.bin", 511705088, 893386752), // 488 MiB, 852 MiB
    MEDIUM("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-medium.bin", 1642824990, 2254857830), // 1.53 GiB, 2.1 GiB
    LARGE("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3.bin", 3328599654, 4187593113), // 3.1 GiB, 3.9 GiB
    LARGE_TURBO("https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-large-v3-turbo.bin", 1739461754, 4187593113), // 1.62 GiB, 3.9 GiB
    ;

    // OpenAI models: https://github.com/openai/whisper/blob/main/whisper/__init__.py#L17-L30
    // GGML models: https://huggingface.co/ggerganov/whisper.cpp/tree/main
    // Memory usage reference: https://github.com/ggml-org/whisper.cpp#memory-usage
}
