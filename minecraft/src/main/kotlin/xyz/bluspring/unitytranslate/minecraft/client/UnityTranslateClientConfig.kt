package xyz.bluspring.unitytranslate.minecraft.client

import kotlinx.serialization.Serializable
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.config.DependsOn
import xyz.bluspring.unitytranslate.common.config.FloatRange
import xyz.bluspring.unitytranslate.common.config.Hidden
import xyz.bluspring.unitytranslate.common.config.IntRange
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.transcriber.whisper.WhisperModel

@Serializable
data class UnityTranslateClientConfig(
    var enabled: Boolean = true,
    var muteTranscriptWhenVoiceChatMuted: Boolean = true,

    val transcriptBoxes: MutableList<TranscriptBoxConfig> = mutableListOf(),

    var transcriber: TranscriberType = TranscriberType.BROWSER,
    @get:DependsOn("transcriber", "WHISPER")
    var whisperModel: WhisperModel = WhisperModel.TINY,
    var browserPath: String = "",

    @get:Hidden
    var spokenLanguage: Language = Language.ENGLISH,
    @get:Hidden
    var balloonLanguage: Language? = null,

    var clientTranslation: ClientTranslation = ClientTranslation.ENABLED_IF_CUDA,

    var disappearingText: Boolean = true,
    @get:DependsOn("disappearingText")
    @get:FloatRange(from = 0.2f, to = 60.0f, increment = 0.1f)
    var disappearingTextDelay: Float = 20.0f,
    @get:DependsOn("disappearingText")
    @get:FloatRange(from = 0.0f, to = 5.0f, increment = 0.1f)
    var disappearingTextFade: Float = 0.5f
) {
    @Serializable
    data class TranscriptBoxConfig(
        var language: Language,

        @get:IntRange(from = 10, to = 300, increment = 10)
        var textScale: Int = 100,

        // value between 0 and 1
        var offsetX: Double = 0.0,
        var offsetY: Double = 0.0,
        var width: Int = 150,
        var height: Int = 120,

        var opacity: Int = 120,
        var color: Int = 0x000000,

        var horizontalAlignType: HorizontalAlignType = HorizontalAlignType.CENTER,
        var verticalAlignType: VerticalAlignType = VerticalAlignType.CENTER,

        var textureLocation: String? = null,
        var headerType: HeaderType = HeaderType.SHORT_LANG
    )

    enum class HeaderType {
        NONE, SHORT_LANG, LONG_LANG;
    }

    enum class HorizontalAlignType {
        LEFT_EDGE, CENTER, RIGHT_EDGE
    }

    enum class VerticalAlignType {
        TOP_EDGE, CENTER, BOTTOM_EDGE
    }

    enum class ClientTranslation {
        ENABLED,
        ENABLED_IF_CUDA,
        DISABLED
    }
}