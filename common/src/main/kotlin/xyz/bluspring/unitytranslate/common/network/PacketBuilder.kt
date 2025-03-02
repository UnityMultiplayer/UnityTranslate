package xyz.bluspring.unitytranslate.common.network

import java.util.*
import kotlin.reflect.KFunction

class PacketBuilder<T : UTPacket>(val id: String, val direction: Direction, val constructor: KFunction<T>) {
    val types = mutableListOf<DataValue<T, *>>()

    fun <V> addType(type: DataType, getter: (T) -> V): PacketBuilder<T> {
        this.types.add(SingleDataValue(type, getter))
        return this
    }

    fun <V : Enum<V>> addEnum(clazz: Class<V>, getter: (T) -> V): PacketBuilder<T> {
        this.types.add(SingleDataValue(DataType.ENUM, getter).apply {
            additionalType = clazz
        })
        return this
    }

    fun <V : Enum<V>> addEnumSet(type: DataType, clazz: Class<V>, getter: (T) -> EnumSet<V>): PacketBuilder<T> {
        this.types.add(SingleDataValue(type, getter).apply {
            additionalType = clazz
        })
        return this
    }

    fun <K, V> addMap(keyType: DataType, valueType: DataType, getter: (T) -> Map<K, V>): PacketBuilder<T> {
        this.types.add(MapDataValue(getter, keyType, valueType))
        return this
    }

    fun <K : Enum<K>, V> addEnumKeyMap(keyClass: Class<K>, valueType: DataType, getter: (T) -> Map<K, V>): PacketBuilder<T> {
        this.types.add(MapDataValue(getter, DataType.ENUM, valueType).apply {
            additionalKeyType = keyClass
        })
        return this
    }

    fun build(vararg args: Any): T {
        return constructor.call(*args)
    }

    interface DataValue<T, V> {
        val type: DataType
        val getter: (T) -> V
    }

    data class SingleDataValue<T, V>(override val type: DataType, override val getter: (T) -> V) : DataValue<T, V> {
        var additionalType: Class<*>? = null
    }

    data class ListDataValue<T, V>(override val getter: (T) -> List<V>, val valueType: DataType) : DataValue<T, List<V>> {
        override val type = DataType.LIST
        var additionalType: Class<*>? = null
    }

    data class MapDataValue<T, K, V>(override val getter: (T) -> Map<K, V>, val keyType: DataType, val valueType: DataType) : DataValue<T, Map<K, V>> {
        override val type = DataType.MAP

        var additionalKeyType: Class<*>? = null
        var additionalValueType: Class<*>? = null
    }

    enum class Direction {
        SERVERBOUND, // C -> S
        CLIENTBOUND, // S -> C
        DUAL // Both clientbound and serverbound, sharing the same datatypes
    }

    enum class DataType {
        VAR_INT,
        VAR_LONG,
        STRING,
        BYTE,
        BOOLEAN,
        UUID,
        ENUM,
        ENUM_SET,
        LIST,
        MAP
    }
}