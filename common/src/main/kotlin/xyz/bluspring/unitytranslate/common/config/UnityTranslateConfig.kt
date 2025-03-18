package xyz.bluspring.unitytranslate.common.config

import kotlinx.serialization.Serializable

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

        var offloadServers: MutableList<OffloadedLibreTranslateServer> = mutableListOf(
            OffloadedLibreTranslateServer("https://libretranslate.devos.gay")
        )
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

    enum class TranslationPriority {
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
    }
}
