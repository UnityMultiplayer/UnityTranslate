package xyz.bluspring.unitytranslate.api.v2.display.text

class MutableTextComponent(
    override val text: String,
    override var style: Style = Style.EMPTY,
    override val siblings: MutableList<TextComponent> = mutableListOf(),
) : TextComponent {
    
}
