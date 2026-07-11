package xyz.bluspring.unitytranslate.client.gui.theme

import xyz.bluspring.unitytranslate.client.config.ColorConfig
import java.awt.Color

object ThemeConfig {
    var mainBackground: ColorConfig = ColorConfig.Gradient(ColorConfig.Gradient.GradientDirection.BOTTOM,
        Color(0x380648).rgb,
        Color(0x130b19).rgb,
    )
    var textColor: Int = Color(0xFFFFFF).rgb

    var dropdownBackground: ColorConfig = ColorConfig.Gradient(ColorConfig.Gradient.GradientDirection.BOTTOM,
        Color(0, 0, 0, 0).rgb,
        Color(0, 0, 0, 89).rgb
    )
    var dropdownOpenBackground: ColorConfig = ColorConfig.Solid(
        Color(0, 0, 0, 230).rgb
    )
    var dropdownOpenOutline: ColorConfig = ColorConfig.Solid(
        Color(255, 255, 255, 25).rgb
    )
    var dropdownTextItemHover: Int = Color(255, 255, 255, 255).rgb
    var dropdownTextItemSelected: Int = Color(255, 255, 0, 255).rgb
    var dropdownTextItemDisabled: Int = Color(100, 100, 100, 255).rgb
    var dropdownTextItem: Int = Color(185, 185, 185, 255).rgb
    var dropdownTextDisabled: Int = Color(190, 190, 190, 255).rgb

    var tooltipBackground: ColorConfig = ColorConfig.Solid(
        Color(0, 0, 0, 220).rgb
    )
    var tooltipText: Int = Color(255, 255, 255, 255).rgb

    var scrollbar: ColorConfig = ColorConfig.Solid(
        Color(255, 255, 255, 255).rgb
    )

    var plainButton: Int = Color(210, 210, 210, 255).rgb
    var plainButtonHover: Int = Color(255, 255, 255, 255).rgb

    var configEntryText: Int = Color(170, 170, 170, 255).rgb
    var configEntryTextFocus: Int = Color(255, 255, 255, 255).rgb

    var toggleOutline: ColorConfig = ColorConfig.Solid(
        Color(170, 170, 170).rgb
    )
    var toggleOutlineFocused: ColorConfig = ColorConfig.Solid(
        Color(255, 255, 255).rgb
    )
    var toggleDisabledFill: ColorConfig = ColorConfig.None
    var toggleEnabledFill: ColorConfig = ColorConfig.Solid(
        Color(255, 255, 255).rgb
    )

    var sliderTrack: ColorConfig = ColorConfig.Solid(
        Color(170, 170, 170).rgb
    )
    var sliderTrackFocused: ColorConfig = ColorConfig.Solid(
        Color(255, 255, 255).rgb
    )
    var sliderNotch: ColorConfig = ColorConfig.Solid(
        Color(170, 170, 170).rgb
    )
    var sliderNotchFocused: ColorConfig = ColorConfig.Solid(
        Color(255, 255, 255).rgb
    )
    var sliderValue: Int = Color(170, 170, 170).rgb
    var sliderValueFocused: Int = Color(255, 255, 255).rgb
}
