package xyz.bluspring.unitytranslate.api.v2.display.text

interface TextComponent {
    val text: String
    val style: Style
    val siblings: List<TextComponent>

    val fullString: String
        get() {
            var current = ""
            this.visit { text -> current += text }

            return current
        }

//    fun flatten(): TextComponent {
//        val texts = mutableListOf<TextComponent>()
//        var currentString = this.string
//        var currentStyle = this.style
//
//        val flattened = DeepRecursiveFunction<TextComponent, List<TextComponent>> { text ->
//            val texts = mutableListOf<TextComponent>(text.plainCopy())
//            for (sibling in text.siblings) {
//                texts.addAll(callRecursive(sibling))
//            }
//
//            texts
//        }(this)
//
//        for (text in flattened.drop(1)) {
//            if (text.style != currentStyle) {
//                texts.add(MutableTextComponent(currentString, currentStyle))
//                currentString = ""
//                currentStyle = text.style
//            }
//
//            currentString += text.string
//        }
//
//        texts.add(MutableTextComponent(currentString, currentStyle))
//    }

    fun visit(visitor: TextVisitor) {
        visitor.visit(this.text)
        for (component in this.siblings) {
            component.visit(visitor)
        }
    }

    fun visit(visitor: StyledTextVisitor, parentStyle: Style) {
        val currentStyle = parentStyle.merge(this.style)
        visitor.visit(this.text, currentStyle)
        for (component in this.siblings) {
            component.visit(visitor, currentStyle)
        }
    }

    fun plainCopy(): MutableTextComponent = MutableTextComponent(this.text)
    fun copy(): MutableTextComponent = MutableTextComponent(this.text, this.style, this.siblings)

    fun interface TextVisitor {
        fun visit(text: String)
    }

    fun interface StyledTextVisitor {
        fun visit(text: String, style: Style)
    }
}
