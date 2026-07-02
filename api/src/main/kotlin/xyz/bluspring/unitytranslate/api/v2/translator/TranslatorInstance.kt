package xyz.bluspring.unitytranslate.api.v2.translator

import com.google.common.collect.HashMultimap
import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.util.LangPair
import java.util.*

abstract class TranslatorInstance {
    abstract suspend fun supportsLanguage(langPair: LangPair): Boolean
    abstract suspend fun batchTranslate(text: List<String>, langPair: LangPair): List<String>
    open suspend fun isAvailable(): Boolean = true

    /**
     * Gets the most efficient translation path based on the translations currently available.
     */
    fun getAvailableTranslationPath(expected: LangPair, available: Collection<LangPair>): List<LangPair> {
        // If there's a direct translation available, just use that.
        if (available.contains(expected) || expected.from == expected.to)
            return listOf(expected)

        // If not, let's try to see if it's possible to do translations via another language.

        // Source lang -> available pairs
        val graph = HashMultimap.create<Language, LangPair>()
        for (pair in available) {
            graph.put(pair.from, pair)
        }

        // Breadth-first search, we want to find the shortest path based on what's available to us right now.
        // Full disclosure: I have had absolutely zero clue what the hell BFS stood for until now.
        val queue = LinkedList<Language>()
        val visited = mutableSetOf<Language>()

        val trackedPath = mutableMapOf<Language, LangPair>()

        queue.add(expected.from)
        visited.add(expected.from)

        var foundPath = false

        while (!queue.isEmpty()) {
            val current = queue.poll()

            if (current == expected.to) {
                foundPath = true
                break
            }

            val nextPairs = graph.get(current)
            for (pair in nextPairs) {
                val nextLang = pair.to

                if (!visited.contains(nextLang)) {
                    visited.add(nextLang)
                    trackedPath[nextLang] = pair
                    queue.add(nextLang)
                }
            }
        }

        if (!foundPath)
            return emptyList()

        val path = LinkedList<LangPair>()
        var current = expected.to

        // Time to link everything together
        while (current != expected.from) {
            val step = trackedPath[current]!!
            path.addFirst(step)
            current = step.from
        }

        return path
    }
}
