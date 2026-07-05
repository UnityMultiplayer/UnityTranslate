package xyz.bluspring.unitytranslate.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class DefaultedDelegate<T>(private val default: KProperty<T>) : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: default.call()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}
