package xyz.bluspring.unitytranslate.api.v2.util

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.library.util.LangPair

data class LangPair(
    val from: Language,
    val to: Language
) {
    constructor(fromCode: String, toCode: String) : this(Language(fromCode), Language(toCode))

    val asLibraryPair: LangPair = LangPair(this.from.languageCode, this.to.languageCode)
}
