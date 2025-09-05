package xyz.bluspring.unitytranslate.gui.config

import kotlinx.serialization.Serializable
import xyz.bluspring.unitytranslate.common.Language
import xyz.bluspring.unitytranslate.common.config.*
import xyz.bluspring.unitytranslate.common.transcriber.TranscriberType
import xyz.bluspring.unitytranslate.common.util.TranslatableEnum
import xyz.bluspring.unitytranslate.gui.transcriber.whisper.WhisperModel

@Serializable
data class UnityTranslateClientConfig(
    var enabled: Boolean = true,
    var muteTranscriptWhenVoiceChatMuted: Boolean = true,

    val transcriptBoxes: MutableList<TranscriptBoxConfig> = mutableListOf(),

    var transcriber: TranscriberType = TranscriberType.BROWSER,

    var whisperModel: WhisperModel = WhisperModel.TINY,
    var browserPath: String = "",

    var spokenLanguage: Language = Language.ENGLISH,
    var balloonLanguage: Language? = null,

    var clientTranslation: ClientTranslation = ClientTranslation.ENABLED_IF_CUDA,

    var disappearingText: Boolean = true,
    //@get:DependsOn("disappearingText")
    //@get:FloatRange(from = 0.2f, to = 60.0f, increment = 0.1f)
    var disappearingTextDelay: Float = 20.0f,
    //@get:DependsOn("disappearingText")
    //@get:FloatRange(from = 0.0f, to = 5.0f, increment = 0.1f)
    var disappearingTextFade: Float = 0.5f,

    var isDarkMode: Boolean = true,
    var backgroundEnabled: Boolean = false,

    val languageProfiles: MutableList<Language?> = ArrayList(5)
) {
    @Serializable
    data class TranscriptBoxConfig(
        var language: Language,

        //@get:IntRange(from = 10, to = 300, increment = 10)
        var textScale: Int = 100,

        // value between 0 and 1
        var offsetX: Float = 0f,
        var offsetY: Float = 0f,
        var width: Int = 150,
        var height: Int = 120,

        var opacity: Int = 120,
        var color: Int = 0x000000,
        var radius: Float = 0f,
        var outlineThickness: Float = 1f,
        var outlineColor: Int = 0x000000,
        var outlineOpacity: Int = 100,

        var horizontalAlignType: HorizontalAlignType = HorizontalAlignType.CENTER,
        var verticalAlignType: VerticalAlignType = VerticalAlignType.CENTER,

        //var textureLocation: String? = null,
        var headerType: HeaderType = HeaderType.SHORT_LANG
    )

    /*enum class BorderEffect(val hasBorder: Boolean, val hasBevel: Boolean) {
        NONE(false, false),
        BORDER(true, false),
        BEVEL(false, true),
        BORDERED_BEVEL(true, true)
    }*/

    enum class HeaderType : TranslatableEnum {
        NONE, SHORT_LANG, LONG_LANG;

        override val translationKey = "config.unitytranslate.client.transcript.header_type.${this.name.lowercase()}"
    }

    enum class HorizontalAlignType {
        LEFT_EDGE, CENTER, RIGHT_EDGE
    }

    enum class VerticalAlignType {
        TOP_EDGE, CENTER, BOTTOM_EDGE
    }

    enum class ClientTranslation : TranslatableEnum {
        ENABLED,
        ENABLED_IF_CUDA,
        DISABLED;

        override val translationKey = "config.unitytranslate.client.clientTranslation.${this.name.lowercase()}"
    }
}