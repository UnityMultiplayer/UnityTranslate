package xyz.bluspring.unitytranslate.api.v2.config

@JvmRecord
data class TranslatableTooltip(
    val translationKey: String,
    val args: List<Any> = emptyList(),
    val color: Int = -1,
)
