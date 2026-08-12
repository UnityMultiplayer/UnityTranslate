package xyz.bluspring.unitytranslate.api.v2.client.gui.element

enum class HorizontalAlign(val adjustment: (Int) -> Float) {
    LEFT({ 0f }), CENTER({ it / 2f }), RIGHT({ it.toFloat() })
}

enum class VerticalAlign(val adjustment: (Int) -> Float) {
    TOP({ 0f }), CENTER({ it / 2f }), BOTTOM({ it.toFloat() })
}
