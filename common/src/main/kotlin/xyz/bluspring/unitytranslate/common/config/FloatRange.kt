package xyz.bluspring.unitytranslate.common.config

@Retention(AnnotationRetention.RUNTIME)
annotation class FloatRange(
    val from: Float,
    val to: Float,
    val increment: Float
)
