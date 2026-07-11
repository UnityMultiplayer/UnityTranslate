package xyz.bluspring.unitytranslate.translator.instance.argos.index

import xyz.bluspring.unitytranslate.api.v2.Language
import xyz.bluspring.unitytranslate.api.v2.util.LangPair

interface ModelPackage {
    val from: Language
    val to: Language

    val langPair: LangPair
        get() = LangPair(from, to)

    val code: String
}
