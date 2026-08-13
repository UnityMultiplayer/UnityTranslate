package xyz.bluspring.unitytranslate.api.v2.display.text

class MutableTextComponent @JvmOverloads constructor(
    override val contents: ComponentContents,
    override var style: Style = Style.EMPTY,
    override val siblings: MutableList<TextComponent> = mutableListOf(),
) : TextComponent {
    fun withStyle(style: Style): MutableTextComponent {
        this.style = this.style.merge(style)
        return this
    }

    fun withStyle(styleBuilder: (Style) -> Style): MutableTextComponent {
        this.style = styleBuilder(this.style)
        return this
    }

    fun withColor(color: Int): MutableTextComponent {
        this.style = this.style.withColor(color)
        return this
    }

    fun append(text: String): MutableTextComponent {
        this.siblings.add(TextComponent.literal(text))
        return this
    }

    fun append(text: TextComponent): MutableTextComponent {
        this.siblings.add(text)
        return this
    }
}
