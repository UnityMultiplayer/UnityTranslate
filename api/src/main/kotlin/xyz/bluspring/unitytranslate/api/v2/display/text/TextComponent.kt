package xyz.bluspring.unitytranslate.api.v2.display.text

/**
 * A variation of text components for UnityTranslate's own usage.
 */
interface TextComponent {
    val contents: ComponentContents
    val style: Style
    val siblings: List<TextComponent>

    val string: String
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
        visitor.visit(this.contents.text)
        for (component in this.siblings) {
            component.visit(visitor)
        }
    }

    fun visit(visitor: StyledTextVisitor, parentStyle: Style = Style.EMPTY) {
        val currentStyle = parentStyle.merge(this.style)
        visitor.visit(this.contents.text, currentStyle)
        for (component in this.siblings) {
            component.visit(visitor, currentStyle)
        }
    }

    fun plainCopy(): MutableTextComponent = MutableTextComponent(this.contents)
    fun copy(): MutableTextComponent = MutableTextComponent(this.contents, this.style, this.siblings.toMutableList())

    fun interface TextVisitor {
        fun visit(text: String)
    }

    fun interface StyledTextVisitor {
        fun visit(text: String, style: Style)
    }

    companion object {
        @JvmStatic fun empty(): MutableTextComponent = MutableTextComponent(ComponentContents.Literal(""))
        @JvmStatic fun literal(text: String): MutableTextComponent = MutableTextComponent(ComponentContents.Literal(text))
        @JvmStatic fun translatable(key: String, vararg args: Any?): MutableTextComponent = MutableTextComponent(ComponentContents.Translatable(key, args.toList()))
        @JvmStatic fun translatableWithFallback(key: String, fallback: String, vararg args: Any?): MutableTextComponent = MutableTextComponent(ComponentContents.Translatable(key, args.toList(), fallback))
    }
}
