package xyz.bluspring.unitytranslate.api.v2.util

/**
 * Reverses a map to be value -> key.
 */
fun <K, V> Map<K, V>.reverse(): Map<V, K> {
    val newMap = mutableMapOf<V, K>()

    for ((key, value) in this) {
        newMap[value] = key
    }

    return newMap
}
