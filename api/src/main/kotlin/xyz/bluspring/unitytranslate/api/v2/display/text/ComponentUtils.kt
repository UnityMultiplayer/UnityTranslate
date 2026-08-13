package xyz.bluspring.unitytranslate.api.v2.display.text

object ComponentUtils {
    @JvmStatic @JvmOverloads
    fun Collection<TextComponent>.join(separator: TextComponent = TextComponent.empty()): TextComponent {
        if (this.isEmpty())
            return TextComponent.empty()

        if (this.size == 1) {
            return this.first()
        }

        val result = TextComponent.empty()
        var isFirst = true

        for (component in this) {
            if (!isFirst) {
                result.append(separator)
            }

            result.append(component)
            isFirst = false
        }

        return result
    }
}
