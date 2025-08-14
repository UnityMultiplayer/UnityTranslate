package xyz.bluspring.unitytranslate.common.config

import kotlinx.serialization.Serializable
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum

@Serializable
data class UnityTranslateConfig(
    var common: CommonConfig = CommonConfig(),
    var server: ServerConfig = ServerConfig(),
) {
    @Serializable
    data class CommonConfig(
        var shouldUseCuda: Boolean = true,

        // Interval for when the batch translations will be sent.
        // This is done so redundant translations don't go through,
        // which puts unnecessary stress on the translation instances.
        @get:FloatRange(from = 0.5f, to = 5.0f, increment = 0.1f)
        var batchTranslateInterval: Float = 0.5f, // 500ms

        var maxConcurrentTranslations: Int = 20,

        // 16384 is capable of fitting every single language of ours while still allowing pretty large text in Vanilla, but we're using
        // 8192 just to be safe.
        // Text should only be segmented anyway in scenarios where we just have yappers creating massive towers
        // of text.
        // The length should only be lowered in scenarios where people are experiencing massive packet issues with these large packets,
        // as the mod should be automatically splitting and joining together these segments by itself. However, the mod should also be very careful about
        // how much data it sends, because too much will cause memory issues.
        //var maxSegmentLength: Int = 8192,

        // 1024 is the max of what *should* be visible at a time, so the translator should only try translating this.
        // It's going to be inaccurate, but it's better than translating 30k characters worth of text and crashing the translator.
        // 1024 is actually being generous honestly, we could make it lower.
        var maxTextLength: Int = 1024,

        var downloadAllTranslationModels: Boolean = false
    )

    @Serializable
    data class ServerConfig(
        var translatePriority: MutableList<TranslationPriority> = mutableListOf(
            TranslationPriority.CLIENT_GPU, // highest priority, prioritize using CUDA on the client-side.
            TranslationPriority.SERVER_GPU, // if supported, use CUDA on the server-side.
            TranslationPriority.SERVER_CPU, // otherwise, translate on the CPU.
            TranslationPriority.OFFLOADED,  // use alternative servers if available
            TranslationPriority.CLIENT_CPU, // worst case scenario, use client CPU.
        ),

        var offloadServers: MutableList<OffloadedLibreTranslateServer> = mutableListOf(),
    )

    @Serializable
    data class OffloadedLibreTranslateServer(
        var url: String, // follows http://127.0.0.1:5000 - the /translate endpoint will be appended at the end automatically.
        var authKey: String? = null,
        var weight: Int = 100,
        var maxConcurrentTranslations: Int = 20
    )

    enum class TriState {
        TRUE, FALSE, DEFAULT
    }

    enum class TranslationPriority : TranslatableEnum {
        SERVER_GPU,
        SERVER_CPU,
        CLIENT_GPU,
        CLIENT_CPU,
        OFFLOADED;

        val isClient: Boolean
            get() = this == CLIENT_GPU || this == CLIENT_CPU

        val isServer: Boolean
            get() = this == SERVER_GPU || this == SERVER_CPU

        val isCuda: Boolean
            get() = this == SERVER_GPU || this == CLIENT_GPU

        override val translationKey = "config.unitytranslate.server.translatePriority.${this.name.lowercase()}"
    }
}
