package xyz.bluspring.unitytranslate.client.gui.screen.config.entry

import net.minecraft.network.chat.Component
import xyz.bluspring.sunset.values.ConfigCategory
import xyz.bluspring.sunset.values.ConfigValue
import xyz.bluspring.unitytranslate.UnityTranslate
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.UIGraphics
import xyz.bluspring.unitytranslate.api.v2.client.gui.element.FadeableUIElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.element.FocusableUIElement
import xyz.bluspring.unitytranslate.api.v2.client.gui.element.UILabel
import xyz.bluspring.unitytranslate.api.v2.client.gui.font.FontReference
import xyz.bluspring.unitytranslate.api.v2.client.theme.ThemeConfig
import xyz.bluspring.unitytranslate.api.v2.client.util.ScreenRectangle
import xyz.bluspring.unitytranslate.client.ClientPlatformProxy
import xyz.bluspring.unitytranslate.config.values.DropdownValidatingReflectingConfigValue
import xyz.bluspring.unitytranslate.config.values.IntColorConfigValue
import xyz.bluspring.unitytranslate.config.values.ValidatingRangedConfigValue
import xyz.bluspring.unitytranslate.config.values.ValidatingReflectingConfigValue
import xyz.bluspring.unitytranslate.util.ScreenUtil
import kotlin.reflect.typeOf

abstract class ConfigEntry<E, T : ConfigValue<E>>(
    val xPos: Float, val yPos: Float,
    val minWidth: Float, val minHeight: Float,
    val value: T,
    val rootKey: String = "",
    val font: FontReference = ClientPlatformProxy.instance.defaultFont,
) : UIElement(), FocusableUIElement, FadeableUIElement {
    protected lateinit var label: UILabel
    protected var shouldShowTooltip = true

    open val width: Float
        get() = this.minWidth

    open val height: Float
        get() = this.minHeight

    override var opacity: Float = 1f
        set(value) {
            field = value
            for (element in this.children) {
                if (element is FadeableUIElement)
                    element.opacity = value
            }
        }

    override fun init(width: Int, height: Int) {
        super.init(width, height)
        this.label = this.addChild(UILabel(this.xPos, this.yPos, Component.translatable("$rootKey${this.value.fullId}").append(": "), font))
    }

    override fun bounds(screenWidth: Int, screenHeight: Int): ScreenRectangle {
        val original = ScreenRectangle(this.xPos.toInt(), this.yPos.toInt(), this.width.toInt(), this.height.toInt())
        return ScreenUtil.union(original, *this.children.map { it.getBounds(screenWidth, screenHeight) }.toTypedArray())
    }

    override fun submit(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val bounds = this.bounds()
        if (bounds.containsPoint(mouseX, mouseY)) {
            this.isFocused = true
            this.label.color = ThemeConfig.configEntryTextFocused
        } else {
            this.label.color = ThemeConfig.configEntryText
        }

        super.submit(graphics, partialTick, mouseX, mouseY)
    }

    override fun submitLate(graphics: UIGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        if (this.bounds().containsPoint(mouseX, mouseY) && this.shouldShowTooltip) {
            val text = Component.translatableWithFallback("${this.rootKey}${this.value.fullId}.description", "")

            if (!text.string.isBlank()) {
                this.tooltip(graphics, text)
            }
        }

        super.submitLate(graphics, partialTick, mouseX, mouseY)
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
            minWidth: Float, minHeight: Float,
            rootKey: String = "", font: FontReference = ClientPlatformProxy.instance.defaultFont,
        ): ConfigEntry<*, *> = when (value) {
            is ValidatingRangedConfigValue -> SliderConfigEntry(xPos, yPos, minWidth, minHeight, value, rootKey, font)
            is DropdownValidatingReflectingConfigValue -> DropdownConfigEntry(xPos, yPos, minWidth, minHeight, value, rootKey, font)
            is IntColorConfigValue -> IntColorConfigEntry(xPos, yPos, minWidth, minHeight, value, rootKey, font)
            is ValidatingReflectingConfigValue -> when (value.type) {
                typeOf<Boolean>() -> ToggleConfigEntry(xPos, yPos, minWidth, minHeight, value as ValidatingReflectingConfigValue<Boolean>, rootKey, font)
                else -> {
                    UnityTranslate.logger.error("Invalid entry type: $value (${value.fullId}, ${value.type})")
                    UnknownConfigValueEntry(xPos, yPos, minWidth, minHeight, rootKey, font, value.fullId)
                }
            }
            is ConfigCategory -> ConfigCategoryEntry(xPos, yPos, minWidth, minHeight, rootKey, font, value)
            else -> {
                UnityTranslate.logger.error("Invalid entry type: $value (${value.fullId}, ${value.type})")
                UnknownConfigValueEntry(xPos, yPos, minWidth, minHeight, rootKey, font, value.fullId)
            }
        }
    }
}
