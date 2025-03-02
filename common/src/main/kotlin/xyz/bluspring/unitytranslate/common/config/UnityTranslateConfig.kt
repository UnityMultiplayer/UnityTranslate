package xyz.bluspring.unitytranslate.common.config

import kotlinx.serialization.Serializable
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType

@Serializable
data class UnityTranslateConfig(
    val client: ClientConfig = ClientConfig(),
    var common: CommonConfig = CommonConfig(),
    var server: ServerConfig = ServerConfig(),
) {
    @Serializable
    data class ClientConfig(
        var enabled: Boolean = true,
        var openBrowserWithoutPrompt: Boolean = false,
        var muteTranscriptWhenVoiceChatMuted: Boolean = true,

        val transcriptBoxes: MutableList<TranscriptBoxConfig> = mutableListOf(),

        @get:Hidden
        val transcriber: TranscriberType = TranscriberType.BROWSER,

        @get:Hidden
        var language: Language = Language.ENGLISH,

        var clientTranslation: ClientTranslation = ClientTranslation.ENABLED_IF_CUDA,

        var disappearingText: Boolean = true,
        @get:DependsOn("disappearingText")
        @get:FloatRange(from = 0.2f, to = 60.0f, increment = 0.1f)
        var disappearingTextDelay: Float = 20.0f,
        @get:DependsOn("disappearingText")
        @get:FloatRange(from = 0.0f, to = 5.0f, increment = 0.1f)
        var disappearingTextFade: Float = 0.5f
    )

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

    @Serializable
    data class TranscriptBoxConfig(
        var language: Language,

        @get:IntRange(from = 10, to = 300, increment = 10)
        var textScale: Int = 100,

        var offsetX: Int = 0,
        var offsetY: Int = 0,
        var width: Int = 150,
        var height: Int = 170,
        var opacity: Int = 120,
        var color: Int = 0x000000,

        var horizontalSnapType: HorizontalSnapType,
        var verticalSnapType: VerticalSnapType
    )

    enum class HorizontalSnapType {
        LEFT_EDGE, CENTER, RIGHT_EDGE
    }

    enum class VerticalSnapType {
        TOP_EDGE, CENTER, BOTTOM_EDGE
    }

    enum class TriState {
        TRUE, FALSE, DEFAULT
    }

    enum class ClientTranslation {
        ENABLED,
        ENABLED_IF_CUDA,
        DISABLED
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
