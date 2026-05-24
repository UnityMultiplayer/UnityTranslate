package xyz.bluspring.unitytranslate.standalone.input

import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import xyz.bluspring.unitytranslate.client.renderer.UnityTranslateGui
import xyz.bluspring.unitytranslate.standalone.UnityTranslateStandalone

object Keyboard {
    fun keyPress(handle: Long, action: Int, event: KeyEvent) {
        if (handle == UnityTranslateStandalone.window.handle()) {
            UnityTranslateGui.keyPress(event.key, 0.toChar(), null)
        }
    }

    fun charTyped(handle: Long, event: CharacterEvent) {
        if (handle == UnityTranslateStandalone.window.handle()) {
            if (Character.isBmpCodePoint(event.codepoint)) {
                UnityTranslateGui.keyPress(0, event.codepoint.toChar(), null)
            } else if (Character.isValidCodePoint(event.codepoint)) {
                UnityTranslateGui.keyPress(0, Character.highSurrogate(event.codepoint), null)
                UnityTranslateGui.keyPress(0, Character.lowSurrogate(event.codepoint), null)
            }
        }
    }
}
