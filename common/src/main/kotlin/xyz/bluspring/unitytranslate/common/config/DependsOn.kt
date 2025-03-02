package xyz.bluspring.unitytranslate.common.config

@Retention(AnnotationRetention.RUNTIME)
annotation class DependsOn(
    val configName: String
)
