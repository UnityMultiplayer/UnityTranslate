package xyz.bluspring.unitytranslate.common.config

import xyz.bluspring.unitytranslate.common.util.TranslatableEnum
import java.nio.file.Path

class ConfigBuilder(val id: String) {
    private val categories = mutableListOf<ConfigBuilder>()

    fun category(id: String, builder: ConfigBuilder.() -> Unit) {
        val config = ConfigBuilder("${this.id}.$id")
        builder.invoke(config)
        categories.add(config)
    }

    fun slider(id: String, min: Int, max: Int, value: Int,
               default: Int = min, increment: Int = 1,
               onChange: (Int) -> Unit, valueTranslator: (Int) -> String
    ) {

    }

    fun slider(id: String, min: Float, max: Float, value: Float,
               default: Float = min, increment: Float = 0.1f,
               onChange: (Float) -> Unit, valueTranslator: (Float) -> String
    ) {

    }

    fun slider(id: String, min: Double, max: Double, value: Double,
               default: Double = min, increment: Double = 0.1,
               onChange: (Double) -> Unit, valueTranslator: (Double) -> String
    ) {

    }

    fun toggle(id: String, value: Boolean,
               default: Boolean = false,
               onChange: (Boolean) -> Unit
    ) {

    }

    fun textInput(id: String, current: String,
                  default: String,
                  onChange: (String) -> Unit
    ) {

    }

    fun filePath(id: String, current: Path?, default: Path? = null, onChange: (Path) -> Unit, extensionFilter: List<String> = emptyList()) {

    }

    fun <T : TranslatableEnum> cycle(id: String, values: List<T>, current: T,
                                     default: T = values[0],
                                     onChange: (T) -> Unit
    ) {

    }

    fun <T : TranslatableEnum> dropdown(id: String, values: List<T>, current: T,
                                        default: T = values[0],
                                        onChange: (T) -> Unit
    ) {

    }
}