package xyz.bluspring.unitytranslate.util

import com.mojang.serialization.MapCodec
import xyz.bluspring.sunset.values.ConfigValue
import kotlin.reflect.KType
import kotlin.reflect.full.createType

class NonConfigValue(id: String) : ConfigValue<Unit>(id, MapCodec.unitCodec(Unit), Unit) {
    override var value: Unit
        get() = Unit
        set(value) {}

    override val type: KType
        get() = Unit::class.createType()
    override val shouldBeSerialized = false
    override fun resetToDefault() {}
}
