package xyz.bluspring.unitytranslate.config.values

import xyz.bluspring.sunset.values.NonConfigValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SeparatorConfigValue : NonConfigValue("separator_${Uuid.generateV7()}") {
}
