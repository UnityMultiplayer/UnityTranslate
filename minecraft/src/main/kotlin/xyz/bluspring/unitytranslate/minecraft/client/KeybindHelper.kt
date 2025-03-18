package xyz.bluspring.unitytranslate.minecraft.client

import net.minecraft.client.KeyMapping
import xyz.bluspring.unitytranslate.minecraft.mixin.client.KeyMappingAccessor

object KeybindHelper {
    private val keybinds = mutableListOf<KeyMapping>()

    fun addCategory(category: String): Boolean {
        val categories = KeyMappingAccessor.getCategorySortOrder()

        if (categories.contains(category))
            return false

        val largestInt = categories.maxOfOrNull { it.value } ?: 0
        categories[category] = largestInt + 1
        return true
    }

    fun register(keybind: KeyMapping): KeyMapping {
        if (keybinds.contains(keybind) || keybinds.any { it.name == keybind.name })
            throw RuntimeException("Keybind already exists: ${keybind.name}")

        addCategory(keybind.category)
        keybinds.add(keybind)
        return keybind
    }

    fun create(original: Array<KeyMapping>): Array<KeyMapping> {
        val newKeys = original.toMutableList()
        newKeys.removeAll(keybinds)
        newKeys.addAll(keybinds)
        return newKeys.toTypedArray()
    }
}