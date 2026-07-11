package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.sunset.values.RangedConfigValue
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.client.gui.element.FocusableUIElement
import xyz.bluspring.unitytranslate.client.gui.element.UIElement
import xyz.bluspring.unitytranslate.client.gui.element.UILabel
import xyz.bluspring.unitytranslate.client.gui.theme.ThemeConfig
import xyz.bluspring.unitytranslate.config.values.DropdownValidatingReflectingConfigValue
import xyz.bluspring.unitytranslate.config.values.ValidatingReflectingConfigValue
import xyz.bluspring.unitytranslate.util.ScreenUtil
import kotlin.reflect.typeOf

abstract class ConfigEntry<E, T : ConfigValue<E>>(
    val xPos: Float, val yPos: Float,
    val width: Float, val height: Float,
    val value: T,
    val rootKey: String = "",
    val font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : UIElement(), FocusableUIElement {
    private lateinit var label: UILabel

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.label = this.addChild(UILabel(this.xPos, this.yPos, Component.translatable("$rootKey${this.value.fullId}"), font))
    }

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        val original = ScreenRectangle(this.xPos.toInt(), this.yPos.toInt(), this.width.toInt(), this.height.toInt())
        return ScreenUtil.union(original, *this.children.map { it.getBounds(screenWidth, screenHeight) }.toTypedArray())
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        if (this.bounds().containsPoint(mouseX, mouseY)) {
            this.isFocused = true
            this.label.color = ThemeConfig.configEntryTextFocus
        } else {
            this.label.color = ThemeConfig.configEntryText
        }

        super.submit(graphics, partialTick, mouseX, mouseY)
    }

    override var isFocused: Boolean = false
        set(value) {
            for (element in this.children) {
                if (element is FocusableUIElement)
                    element.isFocused = value
            }

            field = value
        }

    companion object {
        fun fromValue(
            value: ConfigValue<*>,
            xPos: Float, yPos: Float,
            width: Float, height: Float,
            rootKey: String = "", font: FontReference = ClientPlatformProxy.instance.defaultFont,
        ): ConfigEntry<*, *> = when (value) {
            is RangedConfigValue -> SliderConfigEntry(xPos, yPos, width, height, value, rootKey, font)
            is DropdownValidatingReflectingConfigValue -> DropdownConfigEntry(xPos, yPos, width, height, value, rootKey, font)
            is ValidatingReflectingConfigValue -> when (value.type) {
                typeOf<Boolean>() -> ToggleConfigEntry(xPos, yPos, width, height, value as ValidatingReflectingConfigValue<Boolean>, rootKey, font)
                else -> throw IllegalArgumentException("Invalid entry type: $value (${value.fullId}, ${value.type})")
            }
            else -> throw IllegalArgumentException("Invalid entry type: $value (${value.fullId}, ${value.type})")
        }
    }
}
