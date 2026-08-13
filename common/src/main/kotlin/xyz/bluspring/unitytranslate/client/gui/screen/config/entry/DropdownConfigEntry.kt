package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import xyz.bluspring.unitytranslate.api.v2.client.gui.element.DropdownList
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.config.NameProvidingEntry
import xyz.bluspring.unitytranslate.api.v2.config.TooltipProvidingEntry
import xyz.bluspring.unitytranslate.api.v2.display.text.TextComponent
import xyz.bluspring.unitytranslate.api.v2.download.DownloadableEntry
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.config.builders.ConfigValueBuilderImpl
import xyz.bluspring.unitytranslate.config.values.DropdownValidatingReflectingConfigValue

class DropdownConfigEntry<E : NameProvidingEntry>(
    xPos: Float, yPos: Float,
    width: Float, height: Float,
    value: DropdownValidatingReflectingConfigValue<E>,
    rootKey: String = "",
    font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : ConfigEntry<E, DropdownValidatingReflectingConfigValue<E>>(
    xPos, yPos, width, height, value, rootKey, font
) {
    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.addChild(DropdownList(this.xPos + this.label.bounds().width + 8, this.yPos - 4, this.width, this.height, this.font,
            { this.value.values }, { value ->
                val formatter = (this.value.validator as ConfigValueBuilderImpl<E>).formatter
                if (formatter != null) {
                    formatter(value)
                } else {
                    TextComponent.translatable("${this.rootKey}${this.value.fullId}.${value.serializedName}")
                }
            }, this.value.property,
            validator = { entry ->
                if (entry is DownloadableEntry) {
                    entry.canBeSelected()
                } else (this.value.validator as ConfigValueBuilderImpl<E>).validator(entry)
            },
            tooltip = { entry ->
                (if (entry != null)
                    TextComponent.translatableWithFallback("${this.rootKey}${this.value.fullId}.${entry.serializedName}.description", "")
                else TextComponent.empty()).apply {
                    if (entry is TooltipProvidingEntry) {
                        for (tooltip in entry.tooltip) {
                            if (tooltip.translationKey.isBlank()) {
                                if (!this.string.isBlank())
                                    this.append("\n")

                                continue
                            }

                            if (!this.string.isBlank())
                                this.append("\n")

                            this.append(TextComponent.translatable(tooltip.translationKey, *tooltip.args.toTypedArray()).withColor(tooltip.color))
                        }
                    }
                }
            }
        ))
    }
}
