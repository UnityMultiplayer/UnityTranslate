package xyz.bluspring.unitytranslate.gui.transcriber.whisper

import xyz.bluspring.unitytranslate.common.util.TranslatableEnum

enum class WhisperModel(val modelName: String, val sha256: String, val englishOnly: Boolean = false) : TranslatableEnum {
    TINY_EN("tiny", "d3dd57d32accea0b295c96e26691aa14d8822fac7d9d27d5dc00b4ca2826dd03", true),
    BASE_EN("base", "25a8566e1d0c1e2231d1c762132cd20e0f96a85d16145c3a00adf5d1ac670ead", true),
    SMALL_EN("small", "f953ad0fd29cacd07d5a9eda5624af0f6bcf2258be67c92b79389873d91e0872", true),
    MEDIUM_EN("medium", "d7440d1dc186f76616474e0ff0b3b6b879abc9d1a4926b7adfa41db2d497ab4f", true),
    TINY("tiny", "65147644a518d12f04e32d6f3b26facc3f8dd46e5390956a9424a650c0ce22b9"),
    BASE("base", "ed3a0b6b1c0edf879ad9b11b1af5a0e6ab5db9205f891f668f8b0e6c6326e34e"),
    SMALL("small", "9ecf779972d90ba49c06d968637d720dd632c55bbf19d441fb42bf17a411e794"),
    MEDIUM("medium", "345ae4da62f9b3d59415adc60127b97c714f32e89e936602e85993674d08dcb1"),
    LARGE("large-v3", "e5b1a55b89c1367dacf97e3e19bfd829a01529dbfdeefa8caeb59b3f1b81dadb"),
    TURBO("large-v3-turbo", "aff26ae408abcba5fbf8813c21e62b0941638c5f6eebfb145be0c9839262a19a");

    override val translationKey = "unitytranslate.transcriber.whisper.${this.name.lowercase()}"

    val fileName = "${this.modelName}${if (this.englishOnly) ".en" else ""}.pt"
    val downloadUrl: String = "https://openaipublic.azureedge.net/main/whisper/models/${this.sha256}/$fileName"
}