package xyz.bluspring.unitytranslate.common.util

object TranslateUtil {
    /**
     * Starts from the end of the string, and takes words until the total characters surpass the threshold.
     */
    fun String.takeWordsFromLastUntilMax(threshold: Int): String {
        val combined = mutableListOf<String>()
        var current = 0

        for (string in this.split(" ").reversed()) {
            combined.add(string)
            current += string.length

            if (current >= threshold)
                break
        }

        return combined.reversed().joinToString(" ")
    }
}