package xyz.bluspring.unitytranslate.translator.instance.index

import xyz.bluspring.unitytranslate.api.v2.Language

interface ModelPackage {
    val from: Language
    val to: Language

    val code: String
}
