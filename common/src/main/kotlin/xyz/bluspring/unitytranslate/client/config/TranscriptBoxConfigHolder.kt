package xyz.bluspring.unitytranslate.client.config

import xyz.bluspring.unitytranslate.client.config.TranscriptBoxConfig.*

interface TranscriptBoxConfigHolder {
    var outline: Outline
    var background: Background
    var textColor: Int
    var shadowColor: Int
    var fontScale: Float
    var cornerRadius: Float

    var header: Header
    var transcriptDisplay: TranscriptDisplay

    var padding: Padding
}
