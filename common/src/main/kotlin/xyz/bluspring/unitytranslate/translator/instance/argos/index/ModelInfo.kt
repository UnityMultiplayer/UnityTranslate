package xyz.bluspring.unitytranslate.translator.instance.index

import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import xyz.bluspring.unitytranslate.library.util.TokenizerType
import java.nio.file.Path

@JvmRecord
data class ModelInfo(
    val code: String,
    val langPair: LangPair,

    val translationModelPath: Path,

    val tokenizerType: TokenizerType,
    val tokenizerModelPath: Path,
)
